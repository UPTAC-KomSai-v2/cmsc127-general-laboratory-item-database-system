-- GLIMS database schema.
--
-- This file is hand-maintained (not a mysqldump artifact). Apply with:
--   mysql -u <user> -p < database/schema.sql
--   mysql -u <user> -p genlab_db < database/seed.sql
--
-- There is no automatic schema import on application login (the README used
-- to claim otherwise via a nonexistent "ImportSQLWithJDBC" class -- it never
-- existed in the source). Run these scripts manually before first launch.

CREATE DATABASE IF NOT EXISTS `genlab_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `genlab_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------------
-- Reference tables
-- ---------------------------------------------------------------------------

DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `course`;
CREATE TABLE `course` (
  `course_id` varchar(30) NOT NULL,
  `course_name` varchar(100) NOT NULL,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `course_name` (`course_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `section`;
CREATE TABLE `section` (
  `section_id` int NOT NULL AUTO_INCREMENT,
  `section_name` varchar(50) NOT NULL,
  PRIMARY KEY (`section_id`),
  UNIQUE KEY `section_name` (`section_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `instructor`;
CREATE TABLE `instructor` (
  `instructor_id` int NOT NULL AUTO_INCREMENT,
  `first_name` varchar(50) NOT NULL,
  `surname` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`instructor_id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `course_section`;
CREATE TABLE `course_section` (
  `course_id` varchar(30) NOT NULL,
  `section_id` int NOT NULL,
  `instructor_id` int DEFAULT NULL,
  PRIMARY KEY (`course_id`,`section_id`),
  KEY `section_id` (`section_id`),
  KEY `instructor_id` (`instructor_id`),
  CONSTRAINT `course_section_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `course_section_ibfk_2` FOREIGN KEY (`section_id`) REFERENCES `section` (`section_id`) ON DELETE CASCADE,
  CONSTRAINT `course_section_ibfk_3` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `borrower`;
CREATE TABLE `borrower` (
  `borrower_id` varchar(10) NOT NULL,
  `full_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `contact_number` varchar(13) NOT NULL,
  `degree_prog` enum('BS in Applied Mathematics','BS in Biology','BS in Computer Science') DEFAULT NULL,
  PRIMARY KEY (`borrower_id`),
  UNIQUE KEY `email` (`email`),
  CONSTRAINT `chk_contact_number` CHECK (regexp_like(`contact_number`, _utf8mb4'^(09|\\+639)[0-9]{9}$'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
  `item_id` int NOT NULL AUTO_INCREMENT,
  `item_name` varchar(100) NOT NULL,
  `unit` varchar(20) DEFAULT NULL,
  `qty` int NOT NULL,
  `category_id` int NOT NULL,
  `status` varchar(20) NOT NULL,
  PRIMARY KEY (`item_id`),
  KEY `idx_item_category` (`category_id`),
  KEY `idx_item_status` (`status`),
  FULLTEXT KEY `idx_item_name_search` (`item_name`),
  CONSTRAINT `item_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`) ON DELETE RESTRICT,
  CONSTRAINT `item_chk_1` CHECK (`qty` >= 0),
  CONSTRAINT `item_chk_2` CHECK (`status` in (_utf8mb4'Available',_utf8mb4'Borrowed',_utf8mb4'Reserved',_utf8mb4'Maintenance'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `image`;
CREATE TABLE `image` (
  `image_name` varchar(50) NOT NULL,
  `category_id` int NOT NULL,
  PRIMARY KEY (`image_name`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `image_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `staff_user`;
CREATE TABLE `staff_user` (
  `logID` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `last_login` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`logID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Audit trail for staff-triggered mutations (add/remove item, etc). The
-- application already writes to this table (Queries.logStaffActivity) but
-- the table itself was missing from the schema, so every call silently
-- failed. No foreign keys: a log entry should outlive the row it describes.
DROP TABLE IF EXISTS `staff_activity_log`;
CREATE TABLE `staff_activity_log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) DEFAULT NULL,
  `activity_type` varchar(50) NOT NULL,
  `borrow_id` int DEFAULT NULL,
  `item_id` int DEFAULT NULL,
  `borrower_id` varchar(10) DEFAULT NULL,
  `logged_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Pure ID-minting table. A row is inserted and immediately discarded (kept,
-- actually -- see below) purely to obtain an atomic, collision-free
-- borrow_id via LAST_INSERT_ID(), replacing the old
-- "SELECT MAX(borrow_id)+1" race condition. One borrow_id groups every
-- (borrower, item) row created in a single checkout, so it cannot simply be
-- an AUTO_INCREMENT on `borrow` itself -- that would mint a new id per row
-- instead of once per checkout.
DROP TABLE IF EXISTS `borrow_seq`;
CREATE TABLE `borrow_seq` (
  `seq_id` int NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ---------------------------------------------------------------------------
-- Transactional tables
-- ---------------------------------------------------------------------------

DROP TABLE IF EXISTS `borrow`;
CREATE TABLE `borrow` (
  `borrow_id` int NOT NULL,
  `item_id` int NOT NULL,
  `borrower_id` varchar(10) NOT NULL,
  `course_id` varchar(30) DEFAULT NULL,
  `section_id` int NOT NULL,
  `qty_borrowed` int NOT NULL,
  `date_borrowed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expected_return_date` timestamp NOT NULL DEFAULT ((now() + interval 4 day)),
  `actual_return_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`borrow_id`,`borrower_id`,`item_id`),
  KEY `idx_borrow_item` (`item_id`),
  KEY `idx_borrow_borrower` (`borrower_id`),
  KEY `idx_borrow_course_section` (`course_id`,`section_id`),
  KEY `idx_borrow_dates` (`date_borrowed`,`expected_return_date`,`actual_return_date`),
  CONSTRAINT `borrow_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `item` (`item_id`) ON DELETE CASCADE,
  CONSTRAINT `borrow_ibfk_2` FOREIGN KEY (`borrower_id`) REFERENCES `borrower` (`borrower_id`) ON DELETE CASCADE,
  CONSTRAINT `borrow_ibfk_3` FOREIGN KEY (`course_id`, `section_id`) REFERENCES `course_section` (`course_id`, `section_id`) ON DELETE CASCADE,
  CONSTRAINT `borrow_chk_1` CHECK (`qty_borrowed` >= 0),
  CONSTRAINT `borrow_chk_2` CHECK (`expected_return_date` >= `date_borrowed`),
  CONSTRAINT `borrow_chk_3` CHECK ((`actual_return_date` is null) or (`actual_return_date` >= `date_borrowed`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `return_log`;
CREATE TABLE `return_log` (
  `return_id` int NOT NULL AUTO_INCREMENT,
  `borrow_id` int NOT NULL,
  `return_date` timestamp NOT NULL,
  `item_condition` varchar(100) NOT NULL,
  `late_fee` decimal(10,2) NOT NULL DEFAULT '0.00',
  `borrower_id` varchar(10) NOT NULL,
  `item_id` int NOT NULL,
  `qty_returned` int NOT NULL,
  PRIMARY KEY (`return_id`),
  KEY `idx_return_borrow` (`borrow_id`),
  KEY `return_log_ibfk_2` (`borrow_id`,`borrower_id`,`item_id`),
  CONSTRAINT `return_log_ibfk_2` FOREIGN KEY (`borrow_id`, `borrower_id`, `item_id`) REFERENCES `borrow` (`borrow_id`, `borrower_id`, `item_id`) ON DELETE CASCADE,
  CONSTRAINT `return_log_chk_1` CHECK (`qty_returned` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Triggers
-- ---------------------------------------------------------------------------

DELIMITER ;;

CREATE TRIGGER `after_borrow` AFTER INSERT ON `borrow` FOR EACH ROW BEGIN
    UPDATE item
    SET qty = qty - NEW.qty_borrowed,
        status = CASE
            WHEN (qty - NEW.qty_borrowed) <= 0 THEN 'Borrowed'
            ELSE status
        END
    WHERE item_id = NEW.item_id AND qty >= NEW.qty_borrowed;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot borrow more items than available';
    END IF;
END;;

CREATE TRIGGER `before_return` BEFORE INSERT ON `return_log` FOR EACH ROW BEGIN
    DECLARE expected_date DATE;
    DECLARE days_late INT;

    SELECT expected_return_date INTO expected_date
    FROM borrow
    WHERE borrow_id = NEW.borrow_id AND borrower_id = NEW.borrower_id AND item_id = NEW.item_id;

    SET days_late = DATEDIFF(NEW.return_date, expected_date);

    IF days_late > 0 THEN
        SET NEW.late_fee = days_late * 5.00;
        SET NEW.item_condition = CONCAT(IFNULL(NEW.item_condition, ''), ' (Late return)');
    END IF;
END;;

-- Consolidated return-processing trigger. This replaces the old pair of
-- `after_return` + `update_actual_return` triggers, which independently
-- restored inventory using the FULL `borrow.qty_borrowed` while separately
-- decrementing `qty_borrowed` by only `qty_returned` -- so a partial return
-- always over-credited inventory by (qty_borrowed - qty_returned). This
-- version does both in one place, using `qty_returned` consistently, and
-- fails fast (instead of an opaque CHECK-constraint error) if a return
-- would exceed what is still outstanding.
CREATE TRIGGER `after_return` AFTER INSERT ON `return_log` FOR EACH ROW BEGIN
    DECLARE current_qty_borrowed INT;
    DECLARE remaining_qty INT;

    SELECT qty_borrowed INTO current_qty_borrowed
    FROM borrow
    WHERE borrow_id = NEW.borrow_id AND borrower_id = NEW.borrower_id AND item_id = NEW.item_id
    FOR UPDATE;

    IF current_qty_borrowed IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'No matching borrow record for this return';
    ELSEIF NEW.qty_returned > current_qty_borrowed THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot return more items than are currently outstanding on this borrow';
    END IF;

    -- Computed once, up front: MySQL evaluates multiple SET clauses in a
    -- single UPDATE left-to-right, so a later clause referencing
    -- `qty_borrowed` would see the value an earlier clause in the *same*
    -- UPDATE just wrote, not the pre-update value -- using this variable in
    -- both places below avoids that trap.
    SET remaining_qty = current_qty_borrowed - NEW.qty_returned;

    UPDATE item
    SET qty = qty + NEW.qty_returned,
        status = CASE WHEN (qty + NEW.qty_returned) > 0 THEN 'Available' ELSE status END
    WHERE item_id = NEW.item_id;

    UPDATE borrow
    SET qty_borrowed = remaining_qty,
        actual_return_date = CASE WHEN remaining_qty = 0 THEN NEW.return_date ELSE actual_return_date END
    WHERE borrow_id = NEW.borrow_id AND borrower_id = NEW.borrower_id AND item_id = NEW.item_id;
END;;

DELIMITER ;

-- ---------------------------------------------------------------------------
-- Views
-- ---------------------------------------------------------------------------

CREATE OR REPLACE VIEW `item_availability` AS
SELECT
    i.item_id,
    i.item_name,
    i.qty,
    i.status,
    c.category_name,
    0 AS total_reserved,
    COALESCE(SUM(CASE WHEN b.actual_return_date IS NULL THEN b.qty_borrowed ELSE 0 END), 0) AS total_borrowed,
    (i.qty - COALESCE(SUM(CASE WHEN b.actual_return_date IS NULL THEN b.qty_borrowed ELSE 0 END), 0)) AS available_qty
FROM item i
JOIN category c ON i.category_id = c.category_id
LEFT JOIN borrow b ON i.item_id = b.item_id
GROUP BY i.item_id, i.item_name, i.qty, i.status, c.category_name;

CREATE OR REPLACE VIEW `overdue_items` AS
SELECT
    b.borrow_id,
    i.item_name,
    br.full_name AS borrower_name,
    b.date_borrowed,
    b.expected_return_date,
    (TO_DAYS(CURDATE()) - TO_DAYS(b.expected_return_date)) AS days_overdue,
    rl.late_fee
FROM borrow b
JOIN item i ON b.item_id = i.item_id
JOIN borrower br ON b.borrower_id = br.borrower_id
LEFT JOIN return_log rl ON b.borrow_id = rl.borrow_id
WHERE b.actual_return_date IS NULL AND CURDATE() > b.expected_return_date
ORDER BY days_overdue DESC;

CREATE OR REPLACE VIEW `user_activity_summary` AS
SELECT
    br.borrower_id,
    br.full_name,
    br.email,
    COUNT(DISTINCT b.borrow_id) AS total_borrowings,
    0 AS total_reservations,
    SUM(CASE WHEN b.actual_return_date IS NULL THEN 1 ELSE 0 END) AS active_borrowings,
    SUM(rl.late_fee) AS total_late_fees
FROM borrower br
LEFT JOIN borrow b ON br.borrower_id = b.borrower_id
LEFT JOIN return_log rl ON b.borrow_id = rl.borrow_id
GROUP BY br.borrower_id, br.full_name, br.email;

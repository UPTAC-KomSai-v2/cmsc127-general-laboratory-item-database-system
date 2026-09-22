package com.glims;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the return-processing trigger fix in
 * database/schema.sql. Before this fix, `after_return` restored the FULL
 * borrow.qty_borrowed to inventory on every return (instead of just
 * qty_returned), so a partial return silently over-credited stock.
 */
class ReturnProcessingTest {

    private static final int ITEM_ID = 900001;
    private static final int CATEGORY_ID = 900001;
    private static final String COURSE_ID = "TEST101";
    private static final int SECTION_ID = 900001;
    private static final String BORROWER_ID = "9999-00001";
    private static final int STARTING_QTY = 10;

    private Connection conn;

    @BeforeEach
    void setUp() throws SQLException {
        conn = TestDb.connect();
        conn.setAutoCommit(true);
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM item WHERE item_id = " + ITEM_ID); // cascades borrow -> return_log
            stmt.executeUpdate("DELETE FROM course_section WHERE course_id = '" + COURSE_ID + "'");
            stmt.executeUpdate("DELETE FROM section WHERE section_id = " + SECTION_ID);
            stmt.executeUpdate("DELETE FROM course WHERE course_id = '" + COURSE_ID + "'");
            stmt.executeUpdate("DELETE FROM borrower WHERE borrower_id = '" + BORROWER_ID + "'");
            stmt.executeUpdate("DELETE FROM category WHERE category_id = " + CATEGORY_ID);

            stmt.executeUpdate("INSERT INTO category (category_id, category_name) VALUES (" + CATEGORY_ID + ", 'Test Category')");
            stmt.executeUpdate("INSERT INTO course (course_id, course_name) VALUES ('" + COURSE_ID + "', 'Test Course')");
            stmt.executeUpdate("INSERT INTO section (section_id, section_name) VALUES (" + SECTION_ID + ", 'TestSection900001')");
            stmt.executeUpdate("INSERT INTO course_section (course_id, section_id) VALUES ('" + COURSE_ID + "', " + SECTION_ID + ")");
            stmt.executeUpdate("INSERT INTO borrower (borrower_id, full_name, email, contact_number, degree_prog) " +
                    "VALUES ('" + BORROWER_ID + "', 'Test Borrower', 'test.return.borrower@up.edu.ph', '09170000000', 'BS in Computer Science')");
            stmt.executeUpdate("INSERT INTO item (item_id, item_name, unit, qty, category_id, status) " +
                    "VALUES (" + ITEM_ID + ", 'Test Return Item', NULL, " + STARTING_QTY + ", " + CATEGORY_ID + ", 'Available')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM item WHERE item_id = " + ITEM_ID);
            stmt.executeUpdate("DELETE FROM course_section WHERE course_id = '" + COURSE_ID + "'");
            stmt.executeUpdate("DELETE FROM section WHERE section_id = " + SECTION_ID);
            stmt.executeUpdate("DELETE FROM course WHERE course_id = '" + COURSE_ID + "'");
            stmt.executeUpdate("DELETE FROM borrower WHERE borrower_id = '" + BORROWER_ID + "'");
            stmt.executeUpdate("DELETE FROM category WHERE category_id = " + CATEGORY_ID);
        }
        conn.close();
    }

    private int borrow(int qty) throws SQLException {
        int borrowId;
        try (PreparedStatement seq = conn.prepareStatement("INSERT INTO borrow_seq VALUES ()", Statement.RETURN_GENERATED_KEYS)) {
            seq.executeUpdate();
            try (ResultSet keys = seq.getGeneratedKeys()) {
                keys.next();
                borrowId = keys.getInt(1);
            }
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO borrow(borrow_id, item_id, borrower_id, course_id, section_id, qty_borrowed) VALUES (?, ?, ?, ?, ?, ?)")) {
            ins.setInt(1, borrowId);
            ins.setInt(2, ITEM_ID);
            ins.setString(3, BORROWER_ID);
            ins.setString(4, COURSE_ID);
            ins.setInt(5, SECTION_ID);
            ins.setInt(6, qty);
            ins.executeUpdate();
        }
        return borrowId;
    }

    private void returnItem(int borrowId, int qtyReturned, Timestamp returnDate) throws SQLException {
        try (PreparedStatement ret = conn.prepareStatement(
                "INSERT INTO return_log(borrow_id, borrower_id, item_id, return_date, item_condition, qty_returned) VALUES (?, ?, ?, ?, 'Good', ?)")) {
            ret.setInt(1, borrowId);
            ret.setString(2, BORROWER_ID);
            ret.setInt(3, ITEM_ID);
            ret.setTimestamp(4, returnDate);
            ret.setInt(5, qtyReturned);
            ret.executeUpdate();
        }
    }

    private void returnItem(int borrowId, int qtyReturned) throws SQLException {
        returnItem(borrowId, qtyReturned, new Timestamp(System.currentTimeMillis()));
    }

    private int itemQty() throws SQLException {
        try (PreparedStatement q = conn.prepareStatement("SELECT qty FROM item WHERE item_id = ?")) {
            q.setInt(1, ITEM_ID);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private int qtyBorrowed(int borrowId) throws SQLException {
        try (PreparedStatement q = conn.prepareStatement("SELECT qty_borrowed FROM borrow WHERE borrow_id = ?")) {
            q.setInt(1, borrowId);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private Timestamp actualReturnDate(int borrowId) throws SQLException {
        try (PreparedStatement q = conn.prepareStatement("SELECT actual_return_date FROM borrow WHERE borrow_id = ?")) {
            q.setInt(1, borrowId);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                return rs.getTimestamp(1);
            }
        }
    }

    @Test
    void partialReturnRestoresOnlyReturnedQuantity() throws SQLException {
        int borrowId = borrow(5); // qty: 10 -> 5
        assertEquals(5, itemQty());

        returnItem(borrowId, 2); // return 2 of 5

        // The bug: the old trigger restored the FULL qty_borrowed (5) here,
        // inflating stock to 10 instead of the correct 7.
        assertEquals(7, itemQty());
        assertEquals(3, qtyBorrowed(borrowId));
        assertNull(actualReturnDate(borrowId));
    }

    @Test
    void multiplePartialReturnsSumCorrectlyAndCompleteTheBorrow() throws SQLException {
        int borrowId = borrow(5);

        returnItem(borrowId, 2);
        assertEquals(7, itemQty());
        assertEquals(3, qtyBorrowed(borrowId));
        assertNull(actualReturnDate(borrowId));

        returnItem(borrowId, 3);
        assertEquals(10, itemQty());
        assertEquals(0, qtyBorrowed(borrowId));
        assertNotNull(actualReturnDate(borrowId));
    }

    @Test
    void fullReturnInOneStepRestoresInventoryAndMarksReturned() throws SQLException {
        int borrowId = borrow(4);
        assertEquals(6, itemQty());

        returnItem(borrowId, 4);

        assertEquals(10, itemQty());
        assertEquals(0, qtyBorrowed(borrowId));
        assertNotNull(actualReturnDate(borrowId));
    }

    @Test
    void returningMoreThanOutstandingIsRejected() throws SQLException {
        int borrowId = borrow(5);
        returnItem(borrowId, 2); // 3 remaining

        SQLException ex = assertThrows(SQLException.class, () -> returnItem(borrowId, 4));
        assertTrue(ex.getMessage().contains("Cannot return more items"));

        // State must be unchanged by the rejected return.
        assertEquals(7, itemQty());
        assertEquals(3, qtyBorrowed(borrowId));
    }

    @Test
    void returningAgainAfterFullyReturnedIsRejected() throws SQLException {
        int borrowId = borrow(3);
        returnItem(borrowId, 3); // fully returned
        assertEquals(0, qtyBorrowed(borrowId));

        assertThrows(SQLException.class, () -> returnItem(borrowId, 1));

        assertEquals(10, itemQty()); // unchanged by the rejected duplicate
        assertEquals(0, qtyBorrowed(borrowId));
    }

    @Test
    void itemStatusFlipsBackToAvailableOnceFullyReturned() throws SQLException {
        int borrowId = borrow(STARTING_QTY); // borrow all stock -> 'Borrowed'
        assertEquals(0, itemQty());
        assertEquals("Borrowed", itemStatus());

        returnItem(borrowId, STARTING_QTY);

        assertEquals(STARTING_QTY, itemQty());
        assertEquals("Available", itemStatus());
    }

    @Test
    void lateReturnCalculatesLateFee() throws SQLException {
        int borrowId = borrow(1);
        // borrow_chk_2 requires expected_return_date >= date_borrowed, so we
        // can't just backdate expected_return_date into the past -- instead
        // pin it to the moment of borrowing (due immediately) and return 3
        // days after that.
        Timestamp borrowedAt = new Timestamp(System.currentTimeMillis());
        try (PreparedStatement upd = conn.prepareStatement("UPDATE borrow SET expected_return_date = ? WHERE borrow_id = ?")) {
            upd.setTimestamp(1, borrowedAt);
            upd.setInt(2, borrowId);
            upd.executeUpdate();
        }

        Timestamp returnDate = new Timestamp(borrowedAt.getTime() + 3L * 24 * 60 * 60 * 1000); // 3 days late
        returnItem(borrowId, 1, returnDate);

        try (PreparedStatement q = conn.prepareStatement(
                "SELECT late_fee, item_condition FROM return_log WHERE borrow_id = ?")) {
            q.setInt(1, borrowId);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(15.00, rs.getDouble("late_fee"), 0.001); // 3 days * 5.00
                assertTrue(rs.getString("item_condition").contains("Late return"));
            }
        }
    }

    @Test
    void onTimeReturnHasNoLateFee() throws SQLException {
        int borrowId = borrow(1);
        // expected_return_date defaults to now() + 4 days, so returning now is on time.
        returnItem(borrowId, 1);

        try (PreparedStatement q = conn.prepareStatement(
                "SELECT late_fee FROM return_log WHERE borrow_id = ?")) {
            q.setInt(1, borrowId);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0.00, rs.getDouble("late_fee"), 0.001);
            }
        }
    }

    private String itemStatus() throws SQLException {
        try (PreparedStatement q = conn.prepareStatement("SELECT status FROM item WHERE item_id = ?")) {
            q.setInt(1, ITEM_ID);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }
}

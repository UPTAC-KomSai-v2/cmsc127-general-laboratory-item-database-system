package com.glims;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the borrow-side fixes in Queries.java: the racy
 * "SELECT MAX(borrow_id)+1" ID generation, the INSERT IGNORE that silently
 * swallowed duplicate/invalid borrowers, and the lack of an atomic
 * transaction around a checkout's borrower + item inserts.
 */
class BorrowTransactionTest {

    private static final int ITEM_ID = 900002;
    private static final int CATEGORY_ID = 900002;
    private static final String COURSE_ID = "TEST102";
    private static final int SECTION_ID = 900002;
    private static final String BORROWER_ID = "9999-00002";
    private static final String OTHER_BORROWER_ID = "9999-00003";
    private static final int STARTING_QTY = 5;

    private Queries queries;
    private Connection setupConn;

    @BeforeEach
    void setUp() throws SQLException {
        setupConn = TestDb.connect();
        resetFixtures();

        queries = new Queries();
        assertTrue(queries.login(TestDb.USER, TestDb.PASSWORD, null));
    }

    @AfterEach
    void tearDown() throws SQLException {
        resetFixtures();
        setupConn.close();
    }

    private void resetFixtures() throws SQLException {
        try (Statement stmt = setupConn.createStatement()) {
            stmt.executeUpdate("DELETE FROM item WHERE item_id = " + ITEM_ID); // cascades borrow rows
            stmt.executeUpdate("DELETE FROM course_section WHERE course_id = '" + COURSE_ID + "'");
            stmt.executeUpdate("DELETE FROM section WHERE section_id = " + SECTION_ID);
            stmt.executeUpdate("DELETE FROM course WHERE course_id = '" + COURSE_ID + "'");
            stmt.executeUpdate("DELETE FROM borrower WHERE borrower_id IN ('" + BORROWER_ID + "', '" + OTHER_BORROWER_ID + "')");
            stmt.executeUpdate("DELETE FROM category WHERE category_id = " + CATEGORY_ID);

            stmt.executeUpdate("INSERT INTO category (category_id, category_name) VALUES (" + CATEGORY_ID + ", 'Test Category 2')");
            stmt.executeUpdate("INSERT INTO course (course_id, course_name) VALUES ('" + COURSE_ID + "', 'Test Course 2')");
            stmt.executeUpdate("INSERT INTO section (section_id, section_name) VALUES (" + SECTION_ID + ", 'TestSection900002')");
            stmt.executeUpdate("INSERT INTO course_section (course_id, section_id) VALUES ('" + COURSE_ID + "', " + SECTION_ID + ")");
            stmt.executeUpdate("INSERT INTO item (item_id, item_name, unit, qty, category_id, status) " +
                    "VALUES (" + ITEM_ID + ", 'Test Borrow Item', NULL, " + STARTING_QTY + ", " + CATEGORY_ID + ", 'Available')");
        }
    }

    @Test
    void borrowItemReducesInventoryAndCommitsAtomically() throws SQLException {
        int borrowId = queries.beginBorrowTransaction();
        Queries.BorrowerInsertResult result = queries.insertBorrowerInfo(
                BORROWER_ID, "Test Borrower", "borrow.tx@up.edu.ph", "09170000001", "BS in Computer Science");
        assertEquals(Queries.BorrowerInsertResult.CREATED, result);

        queries.borrowItem(borrowId, ITEM_ID, BORROWER_ID, COURSE_ID, SECTION_ID, 3);
        queries.commitBorrowTransaction();

        assertEquals(2, itemQty());
        assertTrue(borrowerExists(BORROWER_ID));
    }

    @Test
    void insufficientStockRollsBackTheWholeCheckout() throws SQLException {
        int borrowId = queries.beginBorrowTransaction();
        Queries.BorrowerInsertResult result = queries.insertBorrowerInfo(
                OTHER_BORROWER_ID, "Test Borrower", "borrow.tx2@up.edu.ph", "09170000002", "BS in Computer Science");
        assertEquals(Queries.BorrowerInsertResult.CREATED, result);

        assertThrows(SQLException.class, () ->
                queries.borrowItem(borrowId, ITEM_ID, OTHER_BORROWER_ID, COURSE_ID, SECTION_ID, STARTING_QTY + 1));
        queries.rollbackBorrowTransaction();

        assertEquals(STARTING_QTY, itemQty()); // inventory untouched
        // The borrower insert from the same checkout must be rolled back too.
        assertFalse(borrowerExists(OTHER_BORROWER_ID));
    }

    @Test
    void insertBorrowerInfoDistinguishesCreatedReusedAndInvalid() {
        Queries.BorrowerInsertResult created = queries.insertBorrowerInfo(
                OTHER_BORROWER_ID, "New Borrower", "new.borrower@up.edu.ph", "09170000003", "BS in Biology");
        assertEquals(Queries.BorrowerInsertResult.CREATED, created);

        Queries.BorrowerInsertResult reused = queries.insertBorrowerInfo(
                OTHER_BORROWER_ID, "New Borrower", "new.borrower@up.edu.ph", "09170000003", "BS in Biology");
        assertEquals(Queries.BorrowerInsertResult.REUSED, reused);

        Queries.BorrowerInsertResult invalid = queries.insertBorrowerInfo("", "", "", "", "");
        assertEquals(Queries.BorrowerInsertResult.INVALID, invalid);
    }

    @Test
    void concurrentBorrowIdGenerationNeverCollides() throws Exception {
        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                futures.add(pool.submit(this::mintBorrowIdOnFreshConnection));
            }
            Set<Integer> ids = new HashSet<>();
            for (Future<Integer> f : futures) {
                ids.add(f.get());
            }
            assertEquals(threadCount, ids.size(), "every concurrently-minted borrow ID must be unique");
        } finally {
            pool.shutdown();
        }
    }

    private int mintBorrowIdOnFreshConnection() throws SQLException {
        try (Connection c = TestDb.connect();
             PreparedStatement seq = c.prepareStatement("INSERT INTO borrow_seq VALUES ()", Statement.RETURN_GENERATED_KEYS)) {
            seq.executeUpdate();
            try (ResultSet keys = seq.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private int itemQty() throws SQLException {
        try (PreparedStatement q = setupConn.prepareStatement("SELECT qty FROM item WHERE item_id = ?")) {
            q.setInt(1, ITEM_ID);
            try (ResultSet rs = q.executeQuery()) {
                assertTrue(rs.next());
                return rs.getInt(1);
            }
        }
    }

    private boolean borrowerExists(String borrowerId) throws SQLException {
        try (PreparedStatement q = setupConn.prepareStatement("SELECT 1 FROM borrower WHERE borrower_id = ?")) {
            q.setString(1, borrowerId);
            try (ResultSet rs = q.executeQuery()) {
                return rs.next();
            }
        }
    }
}

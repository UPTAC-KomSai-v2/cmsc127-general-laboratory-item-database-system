package com.glims;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Shared connection helper for the correctness test suite. Tests assume
 * database/schema.sql has already been applied to the target database (see
 * README "Getting Started" / .github/workflows/build.yml) -- they do not
 * load the schema themselves, since it contains DELIMITER blocks the JDBC
 * driver can't execute directly.
 */
final class TestDb {
    private TestDb() {}

    // Same GLIMS_DB_URL the application itself reads (Queries.DB_URL), so
    // tests and app code always point at the same database. Username/
    // password are test-only env vars since the app normally takes those
    // from the login screen, which tests don't have.
    static final String URL = System.getenv().getOrDefault("GLIMS_DB_URL", "jdbc:mysql://localhost:3306/genlab_db");
    static final String USER = System.getenv().getOrDefault("GLIMS_TEST_DB_USER", "root");
    static final String PASSWORD = System.getenv().getOrDefault("GLIMS_TEST_DB_PASSWORD", "");

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

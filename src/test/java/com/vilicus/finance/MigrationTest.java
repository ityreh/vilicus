package com.vilicus.finance;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class MigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("vilicus")
            .withUsername("postgres")
            .withPassword("postgres");

    @Test
    void testMigrationsRunSuccessfully() throws Exception {
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .load();

        int migrationsRun = flyway.migrate().migrationsExecuted;
        assertEquals(2, migrationsRun, "Should have run exactly 2 migrations");
    }

    @Test
    void testSchemaIntegrity() throws Exception {
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            DatabaseMetaData metadata = conn.getMetaData();

            // Verify all expected tables exist
            Set<String> expectedTables = Set.of("users", "accounts", "categories", "transactions", "category_rules");
            Set<String> existingTables = new HashSet<>();

            try (ResultSet rs = metadata.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    existingTables.add(rs.getString("TABLE_NAME"));
                }
            }

            for (String table : expectedTables) {
                assertTrue(existingTables.contains(table), "Table '" + table + "' should exist");
            }
        }
    }

    @Test
    void testSeedData() throws Exception {
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (var stmt = conn.createStatement()) {
                try (var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM categories")) {
                    assertTrue(rs.next());
                    int count = rs.getInt("count");
                    assertEquals(15, count, "Should have 15 predefined categories");
                }
            }

            // Verify specific categories exist
            try (var stmt = conn.prepareStatement("SELECT name FROM categories WHERE name = ?")) {
                stmt.setString(1, "Groceries");
                try (var rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Groceries category should exist");
                }
            }

            try (var stmt = conn.prepareStatement("SELECT name FROM categories WHERE name = ?")) {
                stmt.setString(1, "Salary");
                try (var rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Salary category should exist");
                }
            }
        }
    }
}

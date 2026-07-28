package com.revature.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String DB_PATH = "jdbc:sqlite:../database/database.db";
    private static final String SEED_PATH = "../database/seed.sql";
    private static final String SCHEMA_PATH = "../database/schema.sql";
    private static Connection connection;

    private DatabaseConnection() { }

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_PATH);
            initializePragmas(connection);
        }
        return connection;
    }

    private static void initializePragmas(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
        }
    }

    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close database connection", e);
            }
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); 
            Statement stmt = conn.createStatement()) {
            try {
                String schemaSql = new String(Files.readAllBytes(Path.of(SCHEMA_PATH)));
                stmt.executeUpdate(schemaSql);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read schema.sql", e);
            }

            try {
                String seedSql = new String(Files.readAllBytes(Path.of(SEED_PATH)));
                stmt.executeUpdate(seedSql);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read seed.sql", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
        System.out.println("Database initialized successfully.");
    }
}
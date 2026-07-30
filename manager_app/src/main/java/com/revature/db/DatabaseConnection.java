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
    private static final String SCHEMA_PATH = "../database/schema.sql";;

    private DatabaseConnection() { }

    public static synchronized Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_PATH);
        initializePragmas(connection);
        return connection;
    }

    private static void initializePragmas(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
        }
    }

    /*
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close database connection", e);
            }
        }
    }
    */

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); 
            Statement stmt = conn.createStatement()) {
            
            // Drop existing tables to start fresh
            try {
                stmt.execute("DROP TABLE IF EXISTS approvals");
                stmt.execute("DROP TABLE IF EXISTS expenses");
                stmt.execute("DROP TABLE IF EXISTS users");
            } catch (SQLException e) {
                // Tables might not exist, continue
            }
            
            // Read and execute schema
            try {
                String schemaSql = new String(Files.readAllBytes(Path.of(SCHEMA_PATH)));
                // Split by semicolon and execute each statement
                String[] statements = schemaSql.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read schema.sql", e);
            }

            // Read and execute seed data
            try {
                String seedSql = new String(Files.readAllBytes(Path.of(SEED_PATH)));
                // Split by semicolon and execute each statement
                String[] statements = seedSql.split(";");
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
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
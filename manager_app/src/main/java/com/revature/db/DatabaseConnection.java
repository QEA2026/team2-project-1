package com.revature.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        String host = requiredEnvironment("RDSHOST");
        String port = requiredEnvironment("RDS_PORT");
        String database = requiredEnvironment("RDS_DB_NAME");

        Properties properties = new Properties();
        properties.setProperty("user", requiredEnvironment("RDS_USERNAME"));
        properties.setProperty("password", requiredEnvironment("RDS_PASSWORD"));
        properties.setProperty("sslmode", environmentOrDefault("RDS_SSLMODE", "require"));
        properties.setProperty("connectTimeout", environmentOrDefault("RDS_CONNECT_TIMEOUT", "10"));

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(jdbcUrl, properties);
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required database environment variable " + name + " is not set");
        }
        return value;
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

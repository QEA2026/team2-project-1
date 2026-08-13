package com.revature.unit.db;

import com.revature.db.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DatabaseConnectionTest {
    @Test
    void configuredRdsDatabaseIsReachable() {
        assumeTrue(Stream.of("RDSHOST", "RDS_PORT", "RDS_DB_NAME", "RDS_USERNAME", "RDS_PASSWORD")
                        .allMatch(name -> {
                            String value = System.getenv(name);
                            return value != null && !value.isBlank();
                        }),
                "RDS integration settings are not configured");

        assertDoesNotThrow(() -> {
            try (var connection = DatabaseConnection.getConnection();
                 var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT 1")) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
        });
    }
}

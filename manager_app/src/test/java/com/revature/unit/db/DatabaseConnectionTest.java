package com.revature.unit.db;

import com.revature.db.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class DatabaseConnectionTest {
    @Test
    void getConnection_noException() throws SQLException {
        assumeTrue(System.getenv("RDSHOST") != null,
                "RDS integration settings are not configured");
        assertDoesNotThrow(() -> DatabaseConnection.getConnection());
    }
}

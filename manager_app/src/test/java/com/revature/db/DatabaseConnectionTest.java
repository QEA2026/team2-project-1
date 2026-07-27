package com.revature.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseConnectionTest {
    @Test
    void getConnection_noException() throws SQLException {
        assertDoesNotThrow(() -> DatabaseConnection.getConnection());

        try (Connection conn = DatabaseConnection.getConnection()) {
            // TODO
        }
    }
}

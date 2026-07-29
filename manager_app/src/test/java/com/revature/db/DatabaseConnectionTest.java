package com.revature.db;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    @Test
    void getConnection_noException() throws SQLException {
        assertDoesNotThrow(() -> DatabaseConnection.getConnection());
    }
}

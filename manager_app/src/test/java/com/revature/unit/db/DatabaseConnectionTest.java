package com.revature.unit.db;

import com.revature.db.DatabaseConnection;
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

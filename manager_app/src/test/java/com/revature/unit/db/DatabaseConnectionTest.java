package com.revature.unit.db;

import com.revature.db.DatabaseConnection;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    @Test
    @Epic("Unit Testing: Database")
    @Feature("DatabaseConnection")
    @Story("Happy Path: Getting connection from database should not throw an error")
    void getConnection_noException() throws SQLException {
        assertDoesNotThrow(() -> DatabaseConnection.getConnection());
    }
}

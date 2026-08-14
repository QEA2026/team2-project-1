package com.revature.unit.dao;

import com.revature.dao.UserDao;
import com.revature.db.DatabaseConnection;
import com.revature.model.Role;
import com.revature.model.User;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDaoTest {

    private UserDao userDao;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    private String testUsername = "test_user";
    private int testUserId = 1;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDao();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @Epic("Unit Testing: DAO")
    @Feature("UserDAO")
    @Story("Happy Path: Find by username should return user when username exists")
    void findByUsernameShouldReturnUserWhenUsernameExists() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM users WHERE username = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(testUserId);
            when(mockResultSet.getString("username")).thenReturn(testUsername);
            when(mockResultSet.getString("password")).thenReturn("test_password");
            when(mockResultSet.getString("role")).thenReturn("EMPLOYEE");

            Optional<User> result = userDao.findByUsername(testUsername);

            assertTrue(result.isPresent());

            User user = result.get();

            assertEquals(testUsername, user.getUsername());
            assertEquals("test_password", user.getPassword());
            assertEquals(Role.EMPLOYEE, user.getRole());
        }
    }

    @Test
    @Epic("Unit Testing: DAO")
    @Feature("UserDAO")
    @Story("Find by username should return empty when username does not exist")
    void findByUsernameShouldReturnEmptyWhenUsernameDoesNotExist() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM users WHERE username = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Optional<User> result = userDao.findByUsername("missing_user_12345");

            assertTrue(result.isEmpty());
        }
    }

    @Test
    @Epic("Unit Testing: DAO")
    @Feature("UserDAO")
    @Story("Happy Path: Find by id should return user when id exists")
    void findByIdShouldReturnUserWhenIdExists() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM users WHERE id = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(testUserId);
            when(mockResultSet.getString("username")).thenReturn(testUsername);
            when(mockResultSet.getString("password")).thenReturn("test_password");
            when(mockResultSet.getString("role")).thenReturn("EMPLOYEE");

            Optional<User> result = userDao.findById(testUserId);

            assertTrue(result.isPresent());
            assertEquals(testUserId, result.get().getId());
            assertEquals(testUsername, result.get().getUsername());
        }
    }

    @Test
    @Epic("Unit Testing: DAO")
    @Feature("UserDAO")
    @Story("Find by id should return empty when id does not exist")
    void findByIdShouldReturnEmptyWhenIdDoesNotExist() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM users WHERE id = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Optional<User> result = userDao.findById(-999);

            assertTrue(result.isEmpty());
        }
    }
}

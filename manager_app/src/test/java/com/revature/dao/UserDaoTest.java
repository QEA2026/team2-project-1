package com.revature.dao;

import com.revature.db.DatabaseConnection;
import com.revature.model.Role;
import com.revature.model.User;
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

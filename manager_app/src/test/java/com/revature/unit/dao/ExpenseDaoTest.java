package com.revature.unit.dao;

import com.revature.dao.ExpenseDao;
import com.revature.db.DatabaseConnection;
import com.revature.model.PendingExpenseView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExpenseDaoTest {

    private ExpenseDao expenseDao;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    private int pendingExpenseId = 1;
    private int approvedExpenseId = 2;

    @BeforeEach
    void setUp() throws SQLException {
        expenseDao = new ExpenseDao();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    void findPendingShouldReturnPendingExpenses() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("SELECT e.id")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("expense_id")).thenReturn(pendingExpenseId);
            when(mockResultSet.getString("employee_username")).thenReturn("test_user");
            when(mockResultSet.getDouble("amount")).thenReturn(45.75);
            when(mockResultSet.getString("description")).thenReturn("Pending test expense");
            when(mockResultSet.getString("date")).thenReturn("2026-06-20");

            List<PendingExpenseView> results = expenseDao.findPending();

            assertNotNull(results);
            assertFalse(results.isEmpty());

            boolean foundPendingExpense = results.stream()
                    .anyMatch(expense -> expense.getExpenseId() == pendingExpenseId);

            assertTrue(foundPendingExpense);
        }
    }

    @Test
    void findPendingShouldNotReturnApprovedExpenses() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("SELECT e.id")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("expense_id")).thenReturn(pendingExpenseId);
            when(mockResultSet.getString("employee_username")).thenReturn("test_user");
            when(mockResultSet.getDouble("amount")).thenReturn(45.75);
            when(mockResultSet.getString("description")).thenReturn("Pending test expense");
            when(mockResultSet.getString("date")).thenReturn("2026-06-20");

            List<PendingExpenseView> results = expenseDao.findPending();

            boolean foundApprovedExpense = results.stream()
                    .anyMatch(expense -> expense.getExpenseId() == approvedExpenseId);

            assertFalse(foundApprovedExpense);
        }
    }


}
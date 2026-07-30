package com.revature.unit.dao;

import com.revature.dao.ReportDao;
import com.revature.db.DatabaseConnection;
import com.revature.model.ApprovalStatus;
import com.revature.model.ExpenseReportView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportDaoTest {

    private ReportDao reportDao;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        reportDao = new ReportDao();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    void findByEmployeeShouldReturnReportsForUser() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            int userId = 1;
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("SELECT e.id")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("expense_id")).thenReturn(1);
            when(mockResultSet.getString("employee_username")).thenReturn("test_user");
            when(mockResultSet.getDouble("amount")).thenReturn(100.0);
            when(mockResultSet.getString("description")).thenReturn("Test expense");
            when(mockResultSet.getString("date")).thenReturn("2026-06-20");
            when(mockResultSet.getString("status")).thenReturn("PENDING");
            when(mockResultSet.getString("comment")).thenReturn(null);

            List<ExpenseReportView> results = reportDao.findByEmployee(userId);

            assertNotNull(results);
            assertFalse(results.isEmpty());
        }
    }

    @Test
    void findByDateRangeShouldReturnReportsWithinRange() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("SELECT e.id")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("expense_id")).thenReturn(1);
            when(mockResultSet.getString("employee_username")).thenReturn("test_user");
            when(mockResultSet.getDouble("amount")).thenReturn(100.0);
            when(mockResultSet.getString("description")).thenReturn("Test expense");
            when(mockResultSet.getString("date")).thenReturn("2026-06-20");
            when(mockResultSet.getString("status")).thenReturn("PENDING");
            when(mockResultSet.getString("comment")).thenReturn(null);

            List<ExpenseReportView> results = reportDao.findByDateRange(start, end);

            assertNotNull(results);

            for (ExpenseReportView report : results) {
                assertFalse(report.getDate().isBefore(start));
                assertFalse(report.getDate().isAfter(end));
            }
        }
    }

    @Test
    void findByStatusShouldReturnOnlyMatchingStatus() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            ApprovalStatus status = ApprovalStatus.PENDING;
            
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("SELECT e.id")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getInt("expense_id")).thenReturn(1);
            when(mockResultSet.getString("employee_username")).thenReturn("test_user");
            when(mockResultSet.getDouble("amount")).thenReturn(100.0);
            when(mockResultSet.getString("description")).thenReturn("Test expense");
            when(mockResultSet.getString("date")).thenReturn("2026-06-20");
            when(mockResultSet.getString("status")).thenReturn("PENDING");
            when(mockResultSet.getString("comment")).thenReturn(null);

            List<ExpenseReportView> results = reportDao.findByStatus(status);

            assertNotNull(results);

            for (ExpenseReportView report : results) {
                assertEquals(status, report.getStatus());
            }
        }
    }
}
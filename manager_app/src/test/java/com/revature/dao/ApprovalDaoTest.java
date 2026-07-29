package com.revature.dao;

import com.revature.db.DatabaseConnection;
import com.revature.model.Approval;
import com.revature.model.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApprovalDaoTest {

    private ApprovalDao approvalDao;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    private int expenseId = 1;
    private int managerId = 2;

    @BeforeEach
    void setUp() throws SQLException {
        approvalDao = new ApprovalDao();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    void findByExpenseIdShouldReturnApprovalWhenExpenseIdExists() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM approvals WHERE expense_id = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("id")).thenReturn(1);
            when(mockResultSet.getInt("expense_id")).thenReturn(expenseId);
            when(mockResultSet.getString("status")).thenReturn("PENDING");
            when(mockResultSet.getInt("reviewer_id")).thenReturn(0);
            when(mockResultSet.wasNull()).thenReturn(true);
            when(mockResultSet.getString("comment")).thenReturn(null);
            when(mockResultSet.getString("review_date")).thenReturn(null);

            Optional<Approval> result = approvalDao.findByExpenseId(expenseId);

            assertTrue(result.isPresent());

            Approval approval = result.get();

            assertEquals(expenseId, approval.getExpenseId());
            assertEquals(ApprovalStatus.PENDING, approval.getStatus());
            assertNull(approval.getReviewerId());
            assertNull(approval.getComment());
            assertNull(approval.getReviewDate());
        }
    }

    @Test
    void findByExpenseIdShouldReturnEmptyWhenExpenseIdDoesNotExist() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM approvals WHERE expense_id = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Optional<Approval> result = approvalDao.findByExpenseId(-999);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void recordDecisionShouldUpdateApprovalStatusReviewerCommentAndReviewDate() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("UPDATE approvals")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(1);

            approvalDao.recordDecision(
                    expenseId,
                    managerId,
                    ApprovalStatus.APPROVED,
                    "Approved for testing"
            );

            verify(mockConnection).prepareStatement(contains("UPDATE approvals"));
            verify(mockPreparedStatement).setString(1, "APPROVED");
            verify(mockPreparedStatement).setInt(2, managerId);
            verify(mockPreparedStatement).setString(3, "Approved for testing");
            verify(mockPreparedStatement).setInt(5, expenseId);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void recordDecisionShouldThrowExceptionWhenApprovalDoesNotExist() throws SQLException {
        try (var ignored = mockStatic(DatabaseConnection.class)) {
            when(DatabaseConnection.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("UPDATE approvals")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeUpdate()).thenReturn(0);

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> approvalDao.recordDecision(
                            -999,
                            managerId,
                            ApprovalStatus.DENIED,
                            "No matching approval"
                    )
            );

            assertTrue(exception.getMessage().contains("No approval row found"));
        }
    }


}
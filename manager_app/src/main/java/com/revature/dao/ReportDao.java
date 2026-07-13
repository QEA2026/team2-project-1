package com.revature.dao;

import com.revature.db.DatabaseConnection;
import com.revature.model.ApprovalStatus;
import com.revature.model.ExpenseReportView;
import com.revature.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDao implements IReportDao {

    private static final String BASE_SELECT = """
        SELECT e.id AS expense_id,
               u.username AS employee_username,
               e.amount,
               e.description,
               e.date,
               a.status,
               a.comment
        FROM expenses e
        JOIN users u ON u.id = e.user_id
        JOIN approvals a ON a.expense_id = e.id
        """;

    @Override
    public List<ExpenseReportView> findByEmployee(int userId) {
        String sql = BASE_SELECT + " WHERE e.user_id = ? ORDER BY e.date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResults(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch report for user id: " + userId, e);
        }
    }

    @Override
    public List<ExpenseReportView> findByDateRange(LocalDate start, LocalDate end) {
        String sql = BASE_SELECT + " WHERE e.date BETWEEN ? AND ? ORDER BY e.date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start.toString());
            stmt.setString(2, end.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResults(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch report for date range: " + start + " to " + end, e);
        }
    }

    @Override
    public List<ExpenseReportView> findByStatus(ApprovalStatus status) {
        String sql = BASE_SELECT + " WHERE a.status = ? ORDER BY e.date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResults(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch report for status: " + status, e);
        }
    }

    private List<ExpenseReportView> mapResults(ResultSet rs) throws SQLException {
        List<ExpenseReportView> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapRow(rs));
        }
        return results;
    }

    private ExpenseReportView mapRow(ResultSet rs) throws SQLException {
        return new ExpenseReportView(
                rs.getInt("expense_id"),
                rs.getString("employee_username"),
                rs.getDouble("amount"),
                rs.getString("description"),
                DateUtil.parseFlexibleDate(rs.getString("date")),
                ApprovalStatus.valueOf(rs.getString("status")),
                rs.getString("comment")
        );
    }
}
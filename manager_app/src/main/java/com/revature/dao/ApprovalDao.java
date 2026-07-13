package com.revature.dao;

import com.revature.db.DatabaseConnection;
import com.revature.model.Approval;
import com.revature.model.ApprovalStatus;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

import com.revature.util.DateUtil;

public class ApprovalDao implements IApprovalDao {

    @Override
    public Optional<Approval> findByExpenseId(int expenseId) {
        String sql = "SELECT * FROM approvals WHERE expense_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, expenseId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find approval for expense id: " + expenseId, e);
        }
    }

    @Override
    public void recordDecision(int expenseId, int reviewerId, ApprovalStatus status, String comment) {
        String sql = """
            UPDATE approvals
            SET status = ?, reviewer_id = ?, comment = ?, review_date = ?
            WHERE expense_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, reviewerId);
            stmt.setString(3, comment);
            stmt.setString(4, LocalDate.now().toString());
            stmt.setInt(5, expenseId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("No approval row found for expense id: " + expenseId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to record decision for expense id: " + expenseId, e);
        }
    }

    private Approval mapRow(ResultSet rs) throws SQLException {
        int reviewerIdRaw = rs.getInt("reviewer_id");
        Integer reviewerId = rs.wasNull() ? null : reviewerIdRaw;

        String reviewDateStr = rs.getString("review_date");
        LocalDate reviewDate = (reviewDateStr != null)
                ? DateUtil.parseFlexibleDate(reviewDateStr)
                : null;

        return new Approval(
                rs.getInt("id"),
                rs.getInt("expense_id"),
                ApprovalStatus.valueOf(rs.getString("status")),
                reviewerId,
                rs.getString("comment"),
                reviewDate
        );
    }
}
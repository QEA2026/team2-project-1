package com.revature.dao;

import com.revature.db.DatabaseConnection;
import com.revature.model.PendingExpenseView;
import com.revature.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDao implements IExpenseDao {

    @Override
    public List<PendingExpenseView> findPending() {
        String sql = """
            SELECT e.id AS expense_id,
                   u.username AS employee_username,
                   e.amount,
                   e.description,
                   e.date
            FROM expenses e
            JOIN approvals a ON a.expense_id = e.id
            JOIN users u ON u.id = e.user_id
            WHERE a.status = 'PENDING'
            ORDER BY e.date ASC
            """;

        List<PendingExpenseView> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch pending expenses", e);
        }

        return results;
    }

    private PendingExpenseView mapRow(ResultSet rs) throws SQLException {
        return new PendingExpenseView(
                rs.getInt("expense_id"),
                rs.getString("employee_username"),
                rs.getDouble("amount"),
                rs.getString("description"),
                DateUtil.parseFlexibleDate(rs.getString("date"))
        );
    }
}
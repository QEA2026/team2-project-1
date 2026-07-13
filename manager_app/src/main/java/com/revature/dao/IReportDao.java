package com.revature.dao;

import com.revature.model.ApprovalStatus;
import com.revature.model.ExpenseReportView;

import java.time.LocalDate;
import java.util.List;

public interface IReportDao {
    List<ExpenseReportView> findByEmployee(int userId);
    List<ExpenseReportView> findByDateRange(LocalDate start, LocalDate end);
    List<ExpenseReportView> findByStatus(ApprovalStatus status);
}
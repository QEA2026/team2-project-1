package com.revature.model;

import java.time.LocalDate;

public class ExpenseReportView {
    private int expenseId;
    private String employeeUsername;
    private double amount;
    private String description;
    private LocalDate date;
    private ApprovalStatus status;
    private String comment;

    public ExpenseReportView(int expenseId, String employeeUsername, double amount,
                             String description, LocalDate date,
                             ApprovalStatus status, String comment) {
        this.expenseId = expenseId;
        this.employeeUsername = employeeUsername;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.status = status;
        this.comment = comment;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDate() {
        return date;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public String getComment() {
        return comment;
    }
}
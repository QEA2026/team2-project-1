package com.revature.model;

import java.time.LocalDate;

public class PendingExpenseView {
    private int expenseId;
    private String employeeUsername;
    private double amount;
    private String description;
    private LocalDate date;

    public PendingExpenseView(int expenseId, String employeeUsername,
                              double amount, String description, LocalDate date) {
        this.expenseId = expenseId;
        this.employeeUsername = employeeUsername;
        this.amount = amount;
        this.description = description;
        this.date = date;
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
}
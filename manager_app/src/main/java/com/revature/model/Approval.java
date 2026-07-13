package com.revature.model;

import java.time.LocalDate;

public class Approval {
    private int id;
    private int expenseId;
    private ApprovalStatus status;
    private Integer reviewerId; // Integer (not int) since it's nullable in the DB
    private String comment;
    private LocalDate reviewDate;

    public Approval() {}

    public Approval(int id, int expenseId, ApprovalStatus status,
                    Integer reviewerId, String comment, LocalDate reviewDate) {
        this.id = id;
        this.expenseId = expenseId;
        this.status = status;
        this.reviewerId = reviewerId;
        this.comment = comment;
        this.reviewDate = reviewDate;
    }

    public int getId() {
        return id;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }
}
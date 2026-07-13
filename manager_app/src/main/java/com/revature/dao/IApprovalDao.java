package com.revature.dao;

import com.revature.model.Approval;
import com.revature.model.ApprovalStatus;

import java.util.Optional;

public interface IApprovalDao {
    Optional<Approval> findByExpenseId(int expenseId);
    void recordDecision(int expenseId, int reviewerId, ApprovalStatus status, String comment);
}
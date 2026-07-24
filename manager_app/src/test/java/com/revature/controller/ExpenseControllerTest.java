package com.revature.controller;

import com.revature.dao.ApprovalDao;
import com.revature.dao.ExpenseDao;
import com.revature.dao.IApprovalDao;
import com.revature.dao.IExpenseDao;
import com.revature.model.Approval;
import com.revature.model.ApprovalStatus;
import com.revature.model.PendingExpenseView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExpenseControllerTest {

    private IExpenseDao expenseDao;
    private IApprovalDao approvalDao;

    private ExpenseController expenseController;

    @BeforeEach
    void setUp() {
        expenseDao = mock(ExpenseDao.class);
        approvalDao = mock(ApprovalDao.class);

        expenseController = new ExpenseController(expenseDao, approvalDao);
    }

    @Test
    void getPendingWhenNone() {
        when(expenseDao.findPending()).thenReturn(new ArrayList<>());

        List<PendingExpenseView> result = expenseController.getPendingExpenses();

        assertEquals(new ArrayList<>(), result);
    }

    @Test
    void getPendingWhenOne() {
        List<PendingExpenseView> samples = new ArrayList<>();
        samples.add(
                new PendingExpenseView(1, "John Doe", 30.50, "description", LocalDate.of(2020, 1, 1))
        );

        when(expenseDao.findPending()).thenReturn(samples);

        List<PendingExpenseView> result = expenseController.getPendingExpenses();

        assertEquals(samples, result);
    }

    @Test
    void decideExpenseWhenPending() {
        Optional<Approval> sampleApproval = Optional.of(new Approval(1, 1, ApprovalStatus.PENDING, null, null, null));

        when(approvalDao.findByExpenseId(1)).thenReturn(sampleApproval);

        assertDoesNotThrow(() -> expenseController.decideExpense(1, 1, true, "comment"));
    }

    @Test
    void decideExpenseWhenNone() {
        assertThrows(IllegalArgumentException.class, () -> expenseController.decideExpense(1, 1, true, "comment"));
    }

    @Test
    void decideExpenseAlreadyApproved() {
        Optional<Approval> sampleApproval = Optional.of(new Approval(1, 1, ApprovalStatus.APPROVED, null, null, null));

        when(approvalDao.findByExpenseId(1)).thenReturn(sampleApproval);

        assertThrows(IllegalStateException.class, () -> expenseController.decideExpense(1, 1, true, "comment"));
    }
}

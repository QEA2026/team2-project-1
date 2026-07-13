package com.revature.dao;

import com.revature.model.PendingExpenseView;

import java.util.List;

public interface IExpenseDao {
    List<PendingExpenseView> findPending();
    // other methods (findById, findByEmployee, etc.) come later
}
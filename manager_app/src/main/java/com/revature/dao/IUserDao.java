package com.revature.dao;

import com.revature.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserDao {
    Optional<User> findByUsername(String username);

    Optional<User> findById(int id);
}
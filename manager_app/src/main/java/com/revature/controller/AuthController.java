package com.revature.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.revature.model.Role;
import com.revature.model.User;
import com.revature.dao.IUserDao;

import java.util.Optional;

public class AuthController {

    private final IUserDao userDao;

    public AuthController(IUserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new SecurityException("Invalid username or password");
        }

        User user = userOpt.get();

        BCrypt.Result result = BCrypt.verifyer().verify(
                password.toCharArray(),
                user.getPassword().toCharArray()
        );

        if (!result.verified) {
            throw new SecurityException("Invalid username or password");
        }

        if (user.getRole() != Role.MANAGER) {
            throw new SecurityException("Access is restricted to managers");
        }

        return user;
    }
}
package com.revature.controller;

import com.revature.model.Role;
import com.revature.model.User;
import com.revature.service.LoginService;

import java.util.Optional;

public class AuthController {

    private final LoginService loginService;

    public AuthController(LoginService loginService) {
        this.loginService = loginService;
    }

    public User login(String username, String password) {
        Optional<User> userOpt = loginService.login(username, password);

        User user = userOpt.orElseThrow(() ->
                new SecurityException("Invalid username or password"));

        if (user.getRole() != Role.MANAGER) {
            throw new SecurityException("Access is restricted to managers");
        }

        return user;
    }
}
package com.revature.service;

import com.revature.dao.IUserDao;
import com.revature.model.User;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.Optional;

public class LoginService {

    private final IUserDao userDao;

    public LoginService(IUserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> login(String username, String plaintextPassword) {
        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        BCrypt.Result result = BCrypt.verifyer().verify(
                plaintextPassword.toCharArray(),
                user.getPassword().toCharArray()
        );

        if (!result.verified) {
            return Optional.empty();
        }

        return Optional.of(user);
    }
}
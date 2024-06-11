package com.microservice.userservice.service;


import com.microservice.userservice.model.User;

import java.util.List;

public interface UserService {
    public User findUserByJwtToken(String jwt) throws Exception;
    public User findUserByEmail(String email) throws Exception;

    User updatePassword(User user, String password);

    List<User> getAllUsers();
}

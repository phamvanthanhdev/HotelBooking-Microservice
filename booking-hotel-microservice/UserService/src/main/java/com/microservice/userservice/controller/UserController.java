package com.microservice.userservice.controller;

import com.microservice.userservice.dto.UserResponse;
import com.microservice.userservice.model.User;
import com.microservice.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping("/profile")
    private ResponseEntity<UserResponse> findUserByJwtToken(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        UserResponse response = convertUserToResponse(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private UserResponse convertUserToResponse(User user){
        return new UserResponse(user.getFullName(),
                user.getEmail(), user.getRole().name());
    }
}

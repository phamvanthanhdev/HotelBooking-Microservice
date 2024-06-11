package com.microservice.userservice.controller;

import com.microservice.userservice.dto.UserResponse;
import com.microservice.userservice.model.User;
import com.microservice.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/all")
    private ResponseEntity<List<UserResponse>> getAllUser(@RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.findUserByJwtToken(jwt);

        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream().map(this::convertUserToResponse).toList();

        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PutMapping("/update")
    private ResponseEntity<UserResponse> updatePassword(@RequestHeader("Authorization") String jwt,
                                                        @RequestParam String password) throws Exception {
        User user = userService.findUserByJwtToken(jwt);
        User updatedUser = userService.updatePassword(user, password);
        UserResponse response = convertUserToResponse(updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private UserResponse convertUserToResponse(User user){
        return new UserResponse(user.getFullName(),
                user.getEmail(), user.getRole().name());
    }
}

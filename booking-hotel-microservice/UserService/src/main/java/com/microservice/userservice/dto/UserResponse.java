package com.microservice.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservice.userservice.model.USER_ROLE;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String fullName;
    private String email;
    private String role;
}

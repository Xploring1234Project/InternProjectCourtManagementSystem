package com.justiceserve.authenticationservice.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long userId;
    private String name;
    private String email;
    private String password;
    private String role;     // String version of the Enum
    private String status;
}
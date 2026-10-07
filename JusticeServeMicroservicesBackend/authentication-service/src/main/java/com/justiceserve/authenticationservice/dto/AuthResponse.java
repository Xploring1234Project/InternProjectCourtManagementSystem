package com.justiceserve.authenticationservice.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private String role;
}

package com.justiceserve.userservice.dto;

import lombok.Data;

@Data
public class UserResponse {
        private Long userId;
        private String name;
        private String email;
        private String password;
        private String role;
        private String status;
}
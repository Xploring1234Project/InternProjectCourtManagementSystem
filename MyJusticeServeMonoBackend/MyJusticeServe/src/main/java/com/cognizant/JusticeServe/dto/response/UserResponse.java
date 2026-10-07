package com.cognizant.JusticeServe.dto.response;

import com.cognizant.JusticeServe.entity.User;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String name;
    private User.Role role;
    private String email;
    private String phone;
    private User.Status status;
}
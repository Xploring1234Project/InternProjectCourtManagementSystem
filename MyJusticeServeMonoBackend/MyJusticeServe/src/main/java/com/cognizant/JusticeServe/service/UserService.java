package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.response.UserResponse;
import com.cognizant.JusticeServe.entity.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    List<UserResponse> getUsersByRole(User.Role role);
    UserResponse updateUserStatus(Long id, User.Status status);
    void deleteUser(Long id);
    UserResponse updateUserRole(Long id, User.Role role);
}

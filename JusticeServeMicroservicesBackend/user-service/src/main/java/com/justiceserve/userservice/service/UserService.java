package com.justiceserve.userservice.service;

import com.justiceserve.userservice.dto.RegisterRequest;
import com.justiceserve.userservice.dto.UserResponse;
import com.justiceserve.userservice.entity.User;

import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest req);
    UserResponse findByEmail(String email);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    List<UserResponse> getUsersByRole(User.Role role);
    UserResponse updateUserStatus(Long id, User.Status status);
    void deleteUser(Long id);
    UserResponse updateUserRole(Long id, User.Role role);

}
package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.response.UserResponse;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.ResourceNotFoundException;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.service.AuditLogService;
import com.cognizant.JusticeServe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepo.findAll();

        return users.stream()
                .map(user -> modelMapper.map(user,UserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"+id));
        UserResponse response = modelMapper.map(user,UserResponse.class);
        return response;
    }

    @Override
    public List<UserResponse> getUsersByRole(User.Role role) {
        List<User> users = userRepo.findByRole(role);
        return users.stream().map(user -> modelMapper.map(user,UserResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUserStatus(Long id, User.Status status) {
        log.info("Updating user #{} status to {}", id, status);
        User user = userRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("User not found"+id));
        User.Status oldStatus = user.getStatus();
        user.setStatus(status);
        User updatedUser = userRepo.save(user);
        auditLogService.recordSystemAction(id, "USER_STATUS_CHANGED",
                "User:" + id + " [" + oldStatus + " -> " + status + "]");
        return modelMapper.map(updatedUser,UserResponse.class);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user #{}", id);

        User user = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found" + id));
        auditLogService.recordSystemAction(id, "USER_DELETED", "User:" + id + " deleted");

        userRepo.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long id, User.Role role) {
        log.info("Updating user #{} role to {}", id, role);
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        User.Role oldRole = user.getRole();
        user.setRole(role);
        User updatedUser = userRepo.save(user);

        log.info("User #{} role changed: {} -> {}", id, oldRole, role);
        auditLogService.recordSystemAction(id, "USER_ROLE_CHANGED",
                "User:" + id + " - " + user.getName() + " [" + oldRole + " -> " + role + "]");

        return modelMapper.map(updatedUser, UserResponse.class);
    }
}

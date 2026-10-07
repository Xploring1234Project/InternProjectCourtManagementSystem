package com.justiceserve.userservice.service.impl;

import com.justiceserve.userservice.dto.RegisterRequest;
import com.justiceserve.userservice.dto.UserResponse;
import com.justiceserve.userservice.entity.User;
import com.justiceserve.userservice.exception.BadRequestException;
import com.justiceserve.userservice.exception.ResourceNotFoundException;
import com.justiceserve.userservice.feignclient.AuditFeignClient;
import com.justiceserve.userservice.repository.UserRepository;
import com.justiceserve.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditFeignClient auditClient;


    @Override
    public UserResponse register(RegisterRequest req) {

        if(userRepo.existsByEmail(req.getEmail())){
            throw new BadRequestException("Email already exists: " + req.getEmail());
        }

        User user = modelMapper.map(req, User.class);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(User.Role.CITIZEN);
        user.setStatus(User.Status.ACTIVE);

        User savedUser = userRepo.save(user);

        auditClient.log(user.getUserId(), "USER_REGISTERED", "User:"+user.getUserId()+" email="+user.getEmail());

        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse findByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return modelMapper.map(user, UserResponse.class);
    }



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
        auditClient.log(id, "USER_STATUS_CHANGED",
                "User:" + id + " [" + oldStatus + " -> " + status + "]");
        return modelMapper.map(updatedUser,UserResponse.class);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user #{}", id);

        User user = userRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found" + id));
        auditClient.log(id, "USER_DELETED", "User:" + id + " deleted");

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
        auditClient.log(id, "USER_ROLE_CHANGED",
                "User:" + id + " - " + user.getName() + " [" + oldRole + " -> " + role + "]");

        return modelMapper.map(updatedUser, UserResponse.class);
    }
}
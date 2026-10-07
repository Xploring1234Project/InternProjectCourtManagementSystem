package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.response.UserResponse;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.BadRequestException;
import com.cognizant.JusticeServe.exception.ErrorResponse;
import com.cognizant.JusticeServe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // SLF4J Import
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin request to fetch all users in the system");
        List<UserResponse> users = userService.getAllUsers();
        log.debug("Total users retrieved: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Fetching details for User ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN','CLERK','JUDGE')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable User.Role role) {
        log.info("Filtering users by Role: {}", role);
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam User.Status status) {
        log.info("Request to change status of User ID: {} to {}", id, status);
        UserResponse response = userService.updateUserStatus(id, status);
        log.info("Successfully updated status for User ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.warn("DELETION: Request to delete User ID: {}", id);
        userService.deleteUser(id);
        log.info("User ID: {} has been deleted from the system", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String roleStr = body.get("role");

        if (roleStr == null || roleStr.isBlank()) {
            throw new BadRequestException("Role name is required");
        }


        User.Role role = User.Role.valueOf(roleStr.toUpperCase());

        log.info("Request to change Role of User ID: {} to {}", id, role);
        UserResponse response = userService.updateUserRole(id, role);

        return ResponseEntity.ok(response);
    }
}
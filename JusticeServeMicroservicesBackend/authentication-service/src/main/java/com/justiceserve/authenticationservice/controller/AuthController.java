package com.justiceserve.authenticationservice.controller;

import com.justiceserve.authenticationservice.dto.AuthResponse;
import com.justiceserve.authenticationservice.dto.LoginRequest;
import com.justiceserve.authenticationservice.dto.RegisterRequest;
import com.justiceserve.authenticationservice.dto.UserResponse;
import com.justiceserve.authenticationservice.feignclient.UserClient;
import com.justiceserve.authenticationservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserClient userClient;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userClient.registerUser(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.loginUser(req));
    }
}
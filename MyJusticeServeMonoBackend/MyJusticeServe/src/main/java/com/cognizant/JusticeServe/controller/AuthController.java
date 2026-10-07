package com.cognizant.JusticeServe.controller;

import com.cognizant.JusticeServe.dto.request.LoginRequest;
import com.cognizant.JusticeServe.dto.request.RegisterRequest;
import com.cognizant.JusticeServe.dto.response.AuthResponse;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.service.impl.AuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        log.info("User registered successfully with email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt initiated for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}

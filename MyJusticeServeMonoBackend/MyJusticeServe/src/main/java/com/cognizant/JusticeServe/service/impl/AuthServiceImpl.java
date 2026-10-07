package com.cognizant.JusticeServe.service.impl;

import com.cognizant.JusticeServe.dto.request.LoginRequest;
import com.cognizant.JusticeServe.dto.request.RegisterRequest;
import com.cognizant.JusticeServe.dto.response.AuthResponse;
import com.cognizant.JusticeServe.entity.Audit;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.exception.BadRequestException;
import com.cognizant.JusticeServe.repository.UserRepository;
import com.cognizant.JusticeServe.security.JwtService;
import com.cognizant.JusticeServe.service.AuditLogService;
import com.cognizant.JusticeServe.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;


    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for email={}", request.getEmail());

        if(userRepo.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = modelMapper.map(request,User.class);
        // Responsible for encrypting the password and saving to db
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(User.Role.CITIZEN);
        user.setStatus(User.Status.ACTIVE);

        User savedUser = userRepo.save(user);

        log.info("User registered: id={}, name={}, email={}, role=CITIZEN", user.getUserId(), user.getName(), user.getEmail());

        auditLogService.recordSystemAction(savedUser.getUserId(),"USER_REGISTERED","User:" + savedUser.getUserId() + " - " + savedUser.getName() + " (" + savedUser.getEmail() + ") registered as CITIZEN");

        AuthResponse response = modelMapper.map(savedUser,AuthResponse.class);
        response.setToken(jwtService.generateToken(savedUser));
        return response;
    }


    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        Authentication authentication =
                authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));

        User user = userRepo.findByEmail(request.getEmail()).orElseThrow(()-> new BadRequestException("User record not found"));

        log.info("Login successful: userId={}, name={}, role={}", user.getUserId(), user.getName(), user.getRole());

        auditLogService.recordSystemAction(user.getUserId(), "USER_LOGIN",
                "User:" + user.getUserId() + " - " + user.getName() + " logged in with role=" + user.getRole());

        AuthResponse response = modelMapper.map(user,AuthResponse.class);
        String token = jwtService.generateToken(user);
        response.setToken(token);

        return  response;
    }
}



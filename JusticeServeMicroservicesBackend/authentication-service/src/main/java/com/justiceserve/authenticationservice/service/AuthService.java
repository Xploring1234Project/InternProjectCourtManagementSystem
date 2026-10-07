package com.justiceserve.authenticationservice.service;

import com.justiceserve.authenticationservice.dto.AuthResponse;
import com.justiceserve.authenticationservice.dto.LoginRequest;
import com.justiceserve.authenticationservice.dto.UserResponse;
import com.justiceserve.authenticationservice.feignclient.AuditFeignClient;
import com.justiceserve.authenticationservice.feignclient.UserClient;
import com.justiceserve.authenticationservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserClient userClient;
    private final ModelMapper mapper;
    private final AuditFeignClient auditClient;

    public AuthResponse loginUser(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        String email = auth.getName();
        String role = auth.getAuthorities().iterator().next().getAuthority();

        UserResponse user = userClient.getUserByUsername(email);
        AuthResponse response = mapper.map(user, AuthResponse.class);

        response.setToken(jwtService.generateToken(email, role));
        auditClient.log(user.getUserId(), "USER_LOGIN", "User:"+user.getUserId()+" role="+user.getRole());

        return response;
    }
}
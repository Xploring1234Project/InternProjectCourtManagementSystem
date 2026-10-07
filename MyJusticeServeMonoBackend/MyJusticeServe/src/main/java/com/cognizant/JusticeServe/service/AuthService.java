package com.cognizant.JusticeServe.service;

import com.cognizant.JusticeServe.dto.request.LoginRequest;
import com.cognizant.JusticeServe.dto.request.RegisterRequest;
import com.cognizant.JusticeServe.dto.response.AuthResponse;
import com.cognizant.JusticeServe.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

}

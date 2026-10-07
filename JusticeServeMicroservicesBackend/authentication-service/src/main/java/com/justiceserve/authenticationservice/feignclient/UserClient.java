package com.justiceserve.authenticationservice.feignclient;

import com.justiceserve.authenticationservice.dto.RegisterRequest;
import com.justiceserve.authenticationservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="user-service")
public interface UserClient {
    // Changed return type to UserResponse
    @PostMapping("/api/users/register")
    UserResponse registerUser(@RequestBody RegisterRequest req);


    @GetMapping("/api/users/email/{email}")
    UserResponse getUserByUsername(@PathVariable("email") String email);

    @org.springframework.stereotype.Component
    class UserClientImpl implements UserClient {
        @Override
        public UserResponse registerUser(@RequestBody RegisterRequest req) {
            return null;
        }

        @Override
        public UserResponse getUserByUsername(String email) {
            return null;
        }


    }
}
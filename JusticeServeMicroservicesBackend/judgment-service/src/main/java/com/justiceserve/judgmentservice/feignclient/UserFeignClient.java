package com.justiceserve.judgmentservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallback = UserFeignClient.UserFeignFallback.class)
public interface UserFeignClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable Long id);

    record UserDto(Long userId, String name, String email, String phone, String role, String status) {}

    @org.springframework.stereotype.Component
    class UserFeignFallback implements UserFeignClient {
        @Override public UserDto getUserById(Long id) { return null; }
    }
}

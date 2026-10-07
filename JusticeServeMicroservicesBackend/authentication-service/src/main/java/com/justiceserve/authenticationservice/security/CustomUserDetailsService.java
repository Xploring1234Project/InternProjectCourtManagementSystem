package com.justiceserve.authenticationservice.security;

import com.justiceserve.authenticationservice.dto.UserResponse;
import com.justiceserve.authenticationservice.feignclient.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserResponse user = userClient.getUserByUsername(email);

            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + email);
            }

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
            boolean isActive = "ACTIVE".equalsIgnoreCase(user.getStatus());

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    isActive,      // enabled
                    true,          // accountNonExpired
                    true,          // credentialsNonExpired
                    isActive,      // accountNonLocked
                    Collections.singletonList(authority)
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found or service unavailable: " + email);
        }
    }
}
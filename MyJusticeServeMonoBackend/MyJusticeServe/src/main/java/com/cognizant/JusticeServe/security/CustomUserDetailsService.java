package com.cognizant.JusticeServe.security;
import com.cognizant.JusticeServe.entity.User;
import com.cognizant.JusticeServe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found: " + email));

        //Map the role to a GrantedAuthority (adding ROLE_ prefix is best practice)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        //Return Spring's built-in UserDetails implementation
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getStatus() == User.Status.ACTIVE, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                user.getStatus() == User.Status.ACTIVE, // accountNonLocked
                Collections.singletonList(authority)
        );
    }
}

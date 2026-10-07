package com.justiceserve.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/actuator")
                || path.equals("/api/users/register")
                || path.startsWith("/api/users/email/")
                || path.startsWith("/api/audit-logs/internal")
                || path.startsWith("/api/notifications/internal");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /*String path = request.getRequestURI();

        boolean isPublic =
                path.startsWith("/actuator") ||
                        path.startsWith("/api/users/register") ||
                        path.startsWith("/api/users/email/") ||
                        path.startsWith("/api/audit-logs/internal") ||
                        path.startsWith("/api/notifications/internal");

        if (isPublic) {
            filterChain.doFilter(request, response);
            return;
        }*/

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(jwt)) {
                String email = jwtService.extractUserName(jwt);
                String role = jwtService.extractRole(jwt);

                // Since we trust the signed JWT, we don't need a DB trip here.
                // We set the context directly from token claims.

                String authorityRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityRole);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
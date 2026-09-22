package com.job.security;

import com.job.enums.Role;
import com.job.exception.ErrorResponseWriter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ErrorResponseWriter errorResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No token found in Authorization header for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("JWT token received for request: {}", request.getRequestURI());
        String token = authHeader.substring(7);

        String username;
        Long userId;
        Role role;
        try {
            username = jwtService.extractUsername(token);
            userId = jwtService.extractUserId(token);
            role = jwtService.extractRole(token);
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT for request: {}", request.getRequestURI());
            errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
                    "Your session has expired, please log in again", request.getRequestURI());
            return;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Rejected malformed JWT: {} {} ({})", request.getMethod(), request.getRequestURI(),
                    e.getClass().getSimpleName());
            filterChain.doFilter(request, response);
            return;
        }

        if (username == null) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("Username extracted from token: {}", username);

        CustomUserDetails userDetails = CustomUserDetails.fromClaims(userId, username, role);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        log.debug("Security context set for user: {} with role: {}", username, role);

        filterChain.doFilter(request, response);
    }
}

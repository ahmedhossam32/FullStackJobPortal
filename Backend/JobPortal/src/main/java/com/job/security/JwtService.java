package com.job.security;

import com.job.enums.Role;

public interface JwtService {
    String generateToken(Long userId, String username, Role role);

    Long extractUserId(String token);

    String extractUsername(String token);

    Role extractRole(String token);

    boolean isValid(String token);
}

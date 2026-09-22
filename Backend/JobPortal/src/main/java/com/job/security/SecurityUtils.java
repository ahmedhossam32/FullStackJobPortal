package com.job.security;

import com.job.enums.Role;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user in the security context");
        }
        return userDetails;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    public static Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
}

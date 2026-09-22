package com.job.security;

import com.job.entity.User;
import com.job.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Login (fromUser) carries the password hash so AuthenticationManager can verify it.
 * Every other request (fromClaims) carries no password -- JwtAuthFilter never re-fetches the
 * User row, so nothing here should assume the password field is populated outside login.
 */
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final Role role;

    private CustomUserDetails(Long userId, String username, String password, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getRole());
    }

    public static CustomUserDetails fromClaims(Long userId, String username, Role role) {
        return new CustomUserDetails(userId, username, null, role);
    }

    public Long getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

package com.job.security;

import com.job.entity.User;
import com.job.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Used only by AuthenticationManager at login (via DaoAuthenticationProvider). Every other
 * request builds its principal from JWT claims in JwtAuthFilter, not from here.
 */
@Service
@RequiredArgsConstructor
public class JobPortalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return CustomUserDetails.fromUser(user);
    }
}

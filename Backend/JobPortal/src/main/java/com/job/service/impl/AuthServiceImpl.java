package com.job.service.impl;

import com.job.dto.request.LoginRequestDTO;
import com.job.dto.response.AuthResponseDTO;
import com.job.security.CustomUserDetails;
import com.job.security.JwtService;
import com.job.service.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        log.info("Sign-in attempt for username: {}", dto.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Failed sign-in attempt for username: {}", dto.getUsername());
            throw e;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUserId(), userDetails.getUsername(), userDetails.getRole());
        log.info("Sign-in successful for username: {}", dto.getUsername());

        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setRole(userDetails.getRole().name());
        return response;
    }
}

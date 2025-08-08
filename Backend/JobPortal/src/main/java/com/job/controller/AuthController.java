package com.job.controller;

import com.job.dto.EmployerRegisterRequestDTO;
import com.job.dto.JobSeekerRegisterRequestDTO;
import com.job.dto.LoginRequestDTO;
import com.job.dto.LoginResponseDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.enums.Role;
import com.job.security.JwtUtil;
import com.job.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController
{
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup/jobseeker")
    public ResponseEntity<String> signUpJobSeeker(@RequestBody @Valid JobSeekerRegisterRequestDTO dto) {
        try {
            userService.registerJobSeekerWithoutFiles(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Job seeker signed up successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/signup/employer")
    public ResponseEntity<String> signUpEmployer(@RequestBody EmployerRegisterRequestDTO dto) {
        userService.registerEmployer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Employer signed up successfully!");
    }

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO dto) {
        User user = userService.getUserByUsername(dto.getUsername());

        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtUtil.generateToken(user.getUsername());

        if (user.getRole() == Role.EMPLOYER) {
            return ResponseEntity.ok(userService.buildEmployerResponse((Employer) user, token));
        } else {
            return ResponseEntity.ok(userService.buildJobSeekerResponse((JobSeeker) user, token));
        }
    }



}

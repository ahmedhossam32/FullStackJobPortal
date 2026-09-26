package com.job.controller;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.dto.request.LoginRequestDTO;
import com.job.dto.response.AuthResponseDTO;
import com.job.service.AuthService;
import com.job.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/signup/jobseeker")
    public ResponseEntity<String> signUpJobSeeker(@RequestBody @Valid JobSeekerRegisterRequestDTO dto) {
        log.info("Job seeker signup attempt for username: {}", dto.username());
        userService.registerJobSeekerWithoutFiles(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Job seeker signed up successfully!");
    }

    @PostMapping("/signup/employer")
    public ResponseEntity<String> signUpEmployer(@RequestBody @Valid EmployerRegisterRequestDTO dto) {
        log.info("Employer signup attempt for username: {}", dto.username());
        userService.registerEmployer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Employer signed up successfully!");
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}

package com.job.service.impl;

import com.job.dto.request.EmployerRegisterRequestDTO;
import com.job.dto.request.JobSeekerRegisterRequestDTO;
import com.job.dto.response.AuthResponseDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.enums.Role;
import com.job.exception.DuplicateResourceException;
import com.job.exception.ResourceNotFoundException;
import com.job.repository.EmployerRepository;
import com.job.repository.JobSeekerRepository;
import com.job.repository.UserRepository;
import com.job.service.interfaces.IUserService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public JobSeeker registerJobSeekerWithoutFiles(JobSeekerRegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        log.info("Registering new job seeker: {}", dto.getUsername());
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setName(dto.getName());
        jobSeeker.setUsername(dto.getUsername());
        jobSeeker.setPassword(passwordEncoder.encode(dto.getPassword()));
        jobSeeker.setDob(dto.getDob());
        jobSeeker.setRole(Role.JOB_SEEKER);
        jobSeeker.setEmail(dto.getEmail());

        // Skip resume and profile picture
        jobSeeker.setResumeFileName(null);
        jobSeeker.setProfilePictureFileName(null);

        return jobSeekerRepository.save(jobSeeker);
    }

    @Override
    public Employer registerEmployer(EmployerRegisterRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        log.info("Registering new employer: {}", dto.getUsername());
        Employer employer = new Employer();
        employer.setName(dto.getName());
        employer.setUsername(dto.getUsername());
        employer.setPassword(passwordEncoder.encode(dto.getPassword()));
        employer.setCompanyName(dto.getCompanyName());
        employer.setProfilePictureFileName(dto.getProfilePictureFileName());
        employer.setRole(Role.EMPLOYER);
        employer.setEmail(dto.getEmail());
        employer.setIndustry(dto.getIndustry());
        return employerRepository.save(employer);
    }

    @Override
    public User getUserByUsername(@NotBlank(message = "Username is required") String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public AuthResponseDTO buildAuthResponse(User user, String token) {
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setRole(user.getRole().name());
        return response;
    }

    public boolean authenticate(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public JobSeeker registerJobSeeker(JobSeekerRegisterRequestDTO dto, MultipartFile resume, MultipartFile profilePicture) throws IOException {
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setName(dto.getName());
        jobSeeker.setUsername(dto.getUsername());
        jobSeeker.setPassword(passwordEncoder.encode(dto.getPassword()));
        jobSeeker.setDob(dto.getDob());
        jobSeeker.setRole(Role.JOB_SEEKER);
        jobSeeker.setEmail(dto.getEmail());

        if (resume != null && !resume.isEmpty()) {
            String originalFilename = resume.getOriginalFilename();
            String resumeName = saveFile(resume, "uploads/resumes/");
            jobSeeker.setResumeFileName(resumeName);
            jobSeeker.setResumeOriginalName(originalFilename);
        }

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String pictureName = saveFile(profilePicture, "uploads/profile-pictures/");
            jobSeeker.setProfilePictureFileName(pictureName);
        }

        return jobSeekerRepository.save(jobSeeker);
    }

    private String saveFile(MultipartFile file, String uploadDir) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }
}

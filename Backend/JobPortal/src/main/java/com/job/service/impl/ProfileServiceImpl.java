package com.job.service.impl;

import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.dto.response.EmployerProfileDTO;
import com.job.dto.response.JobSeekerProfileDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.enums.Role;
import com.job.exception.BadRequestException;
import com.job.exception.DuplicateResourceException;
import com.job.exception.ResourceNotFoundException;
import com.job.repository.JobSeekerRepository;
import com.job.repository.UserRepository;
import com.job.service.ProfileService;
import com.job.validation.FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final CloudinaryService cloudinaryService;
    private final FileValidator fileValidator;

    @Override
    @Transactional
    public String uploadResume(MultipartFile file, Long jobSeekerId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));
        fileValidator.validateResume(file);
        log.info("Uploading resume for user: {}, file: {}", jobSeeker.getUsername(), file.getOriginalFilename());
        String url = cloudinaryService.uploadResume(file);
        jobSeeker.setResumeUrl(url);
        userRepository.save(jobSeeker);
        return url;
    }

    @Override
    @Transactional
    public String uploadProfilePicture(MultipartFile file, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        fileValidator.validateImage(file);
        log.info("Uploading profile picture for user: {}, file: {}", user.getUsername(), file.getOriginalFilename());
        String url = cloudinaryService.uploadImage(file);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return url;
    }

    @Override
    @Transactional
    public void updateJobSeekerProfile(Long jobSeekerId, UpdateProfileRequestDTO updatedInfo) {
        JobSeeker currentUser = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));
        log.info("Updating profile for job seeker: {}", currentUser.getUsername());

        if (!updatedInfo.getUsername().equals(currentUser.getUsername())
                && userRepository.existsByUsername(updatedInfo.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (!updatedInfo.getEmail().equals(currentUser.getEmail())
                && userRepository.existsByEmail(updatedInfo.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        currentUser.setName(updatedInfo.getName());
        currentUser.setUsername(updatedInfo.getUsername());
        currentUser.setEmail(updatedInfo.getEmail());
        currentUser.setDob(updatedInfo.getDob());
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getCurrentUserDto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.JOB_SEEKER && user instanceof JobSeeker jobSeeker) {
            JobSeekerProfileDTO dto = new JobSeekerProfileDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setName(user.getName());
            dto.setRole(user.getRole().name());
            dto.setProfilePicture(user.getProfilePictureUrl());
            dto.setResume(jobSeeker.getResumeUrl());
            dto.setResumeOriginalName(jobSeeker.getResumeOriginalName());
            dto.setDob(jobSeeker.getDob());
            return dto;
        }

        if (user.getRole() == Role.EMPLOYER && user instanceof Employer employer) {
            EmployerProfileDTO dto = new EmployerProfileDTO();
            dto.setId(employer.getId());
            dto.setUsername(employer.getUsername());
            dto.setEmail(employer.getEmail());
            dto.setName(employer.getName());
            dto.setRole(employer.getRole().name());
            dto.setCompanyName(employer.getCompanyName());
            dto.setIndustry(employer.getIndustry());
            dto.setProfilePicture(employer.getProfilePictureUrl());
            return dto;
        }

        throw new BadRequestException("Unsupported user role");
    }
}

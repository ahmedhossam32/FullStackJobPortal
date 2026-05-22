package com.job.service.impl;

import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.dto.response.EmployerProfileDTO;
import com.job.dto.response.JobSeekerProfileDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.enums.Role;
import com.job.exception.BadRequestException;
import com.job.repository.UserRepository;
import com.job.service.interfaces.IProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public String uploadResume(MultipartFile file, JobSeeker jobSeeker) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must not exceed 5MB");
        }
        log.info("Uploading resume for user: {}, file: {}", jobSeeker.getUsername(), file.getOriginalFilename());
        String url = cloudinaryService.uploadResume(file);
        jobSeeker.setResumeUrl(url);
        userRepository.save(jobSeeker);
        return url;
    }

    @Override
    @Transactional
    public String uploadProfilePicture(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size must not exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
        log.info("Uploading profile picture for user: {}, file: {}", user.getUsername(), file.getOriginalFilename());
        String url = cloudinaryService.uploadImage(file);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return url;
    }

    @Override
    @Transactional
    public void updateJobSeekerProfile(JobSeeker currentUser, UpdateProfileRequestDTO updatedInfo) {
        log.info("Updating profile for job seeker: {}", currentUser.getUsername());
        currentUser.setName(updatedInfo.getName());
        currentUser.setUsername(updatedInfo.getUsername());
        currentUser.setEmail(updatedInfo.getEmail());
        currentUser.setDob(updatedInfo.getDob());
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getCurrentUserDto(User user) {
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

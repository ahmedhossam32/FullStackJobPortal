package com.job.service.impl;

import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.dto.response.ProfileResponseDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.enums.Role;
import com.job.exception.BadRequestException;
import com.job.exception.DuplicateResourceException;
import com.job.exception.ResourceNotFoundException;
import com.job.mapper.ProfileMapper;
import com.job.repository.JobSeekerRepository;
import com.job.repository.UserRepository;
import com.job.service.ProfileService;
import com.job.validation.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final CloudinaryService cloudinaryService;
    private final FileValidator fileValidator;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public String uploadResume(MultipartFile file, Long jobSeekerId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));
        fileValidator.validateResume(file);
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

        if (!updatedInfo.username().equals(currentUser.getUsername())
                && userRepository.existsByUsername(updatedInfo.username())) {
            throw new DuplicateResourceException("Username already taken");
        }
        if (!updatedInfo.email().equals(currentUser.getEmail())
                && userRepository.existsByEmail(updatedInfo.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        currentUser.setName(updatedInfo.name());
        currentUser.setUsername(updatedInfo.username());
        currentUser.setEmail(updatedInfo.email());
        currentUser.setDob(updatedInfo.dob());
        userRepository.save(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getCurrentUserDto(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.JOB_SEEKER && user instanceof JobSeeker jobSeeker) {
            return profileMapper.toJobSeekerDTO(jobSeeker);
        }

        if (user.getRole() == Role.EMPLOYER && user instanceof Employer employer) {
            return profileMapper.toEmployerDTO(employer);
        }

        throw new BadRequestException("Unsupported user role");
    }
}

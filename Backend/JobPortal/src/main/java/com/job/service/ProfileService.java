package com.job.service;

import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.dto.response.ProfileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    String uploadResume(MultipartFile file, Long jobSeekerId);
    String uploadProfilePicture(MultipartFile file, Long userId);
    void updateJobSeekerProfile(Long jobSeekerId, UpdateProfileRequestDTO updatedInfo);
    ProfileResponseDTO getCurrentUserDto(Long userId);
}

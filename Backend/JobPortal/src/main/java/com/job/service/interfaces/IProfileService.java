package com.job.service.interfaces;

import com.job.dto.request.UpdateProfileRequestDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {
    String uploadResume(MultipartFile file, Long jobSeekerId);
    String uploadProfilePicture(MultipartFile file, Long userId);
    void updateJobSeekerProfile(Long jobSeekerId, UpdateProfileRequestDTO updatedInfo);
    Object getCurrentUserDto(Long userId);
}
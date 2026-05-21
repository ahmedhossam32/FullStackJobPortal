package com.job.service.interfaces;

import com.job.entity.JobSeeker;
import com.job.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface IProfileService {
    String uploadResume(MultipartFile file, JobSeeker jobSeeker);
    String uploadProfilePicture(MultipartFile file, User user);
    void updateJobSeekerProfile(JobSeeker currentUser, JobSeeker updatedInfo);
    Object getCurrentUserDto(User user);
}
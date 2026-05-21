package com.job.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {

    private final UserRepository userRepository;

    @Override
    public String uploadResume(MultipartFile file, JobSeeker jobSeeker) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "resumes" + File.separator;
            File dest = new File(uploadDir + fileName);

            dest.getParentFile().mkdirs(); // Create dir if missing
            file.transferTo(dest);

            jobSeeker.setResumeFileName(fileName);
            userRepository.save(jobSeeker);

            return "/uploads/resumes/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Resume upload failed", e);
        }
    }

    @Override
    public String uploadProfilePicture(MultipartFile file, User user) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "profile-pictures" + File.separator;
            File dest = new File(uploadDir + fileName);

            dest.getParentFile().mkdirs();
            file.transferTo(dest);

            user.setProfilePictureFileName(fileName);
            userRepository.save(user);
            return "/uploads/profile-pictures/" + fileName;
        } catch (IOException e) {
            throw new BadRequestException("Profile picture upload failed", e);
        }
    }

    @Override
    public void updateJobSeekerProfile(JobSeeker currentUser, JobSeeker updatedInfo) {
        currentUser.setName(updatedInfo.getName());
        currentUser.setUsername(updatedInfo.getUsername());
        currentUser.setEmail(updatedInfo.getEmail());
        currentUser.setDob(updatedInfo.getDob());
        userRepository.save(currentUser);
    }

    @Override
    public Object getCurrentUserDto(User user) {
        if (user.getRole() == Role.JOB_SEEKER && user instanceof JobSeeker jobSeeker) {
            JobSeekerProfileDTO dto = new JobSeekerProfileDTO();
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setName(user.getName());
            dto.setRole(user.getRole().name());
            dto.setProfilePicture(user.getProfilePictureFileName());
            dto.setResume(jobSeeker.getResumeFileName());
            dto.setResumeOriginalName(jobSeeker.getResumeOriginalName());
            dto.setDob(jobSeeker.getDob());
            return dto;
        }

        if (user.getRole() == Role.EMPLOYER && user instanceof Employer employer) {
            EmployerProfileDTO dto = new EmployerProfileDTO();
            dto.setUsername(employer.getUsername());
            dto.setEmail(employer.getEmail());
            dto.setName(employer.getName());
            dto.setRole(employer.getRole().name());
            dto.setCompanyName(employer.getCompanyName());
            dto.setIndustry(employer.getIndustry());
            dto.setProfilePicture(employer.getProfilePictureFileName());
            return dto;
        }

        throw new BadRequestException("Unsupported user role");
    }
}
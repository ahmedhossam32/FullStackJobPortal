package com.job.controller;

import com.job.dto.request.UpdateProfileRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.security.SecurityUtils;
import com.job.service.interfaces.IProfileService;
import com.job.service.interfaces.ISavedJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final IProfileService profileService;
    private final ISavedJobService savedJobService;

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PostMapping("/jobseeker/upload-resume")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        String resumeUrl = profileService.uploadResume(file, jobSeekerId);
        return ResponseEntity.ok("Resume uploaded successfully: " + resumeUrl);
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        String profileUrl = profileService.uploadProfilePicture(file, userId);
        return ResponseEntity.ok("Profile picture uploaded successfully: " + profileUrl);
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PostMapping("/save-job/{jobId}")
    public ResponseEntity<String> saveJob(@PathVariable Long jobId) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        savedJobService.saveJob(jobSeekerId, jobId);
        return ResponseEntity.ok("Job saved successfully.");
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PutMapping("/jobseeker/update-profile")
    public ResponseEntity<String> updateProfile(@RequestBody @Valid UpdateProfileRequestDTO updatedInfo) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        profileService.updateJobSeekerProfile(jobSeekerId, updatedInfo);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @DeleteMapping("/unsave-job/{jobId}")
    public ResponseEntity<String> unsaveJob(@PathVariable Long jobId) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        savedJobService.unsaveJob(jobSeekerId, jobId);
        return ResponseEntity.ok("Job removed from saved list.");
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/saved-jobs")
    public ResponseEntity<List<JobResponseDTO>> getSavedJobs() {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(savedJobService.getSavedJobs(jobSeekerId));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(profileService.getCurrentUserDto(userId));
    }
}

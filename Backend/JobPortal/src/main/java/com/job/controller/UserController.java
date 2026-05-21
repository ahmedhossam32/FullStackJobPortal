package com.job.controller;

import com.job.dto.response.JobResponseDTO;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController
{
    private final UserService userService;

    @PostMapping("/jobseeker/upload-resume")
    public ResponseEntity<String> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("user") JobSeeker jobSeeker
    ) {
        String resumeUrl = userService.uploadResume(file, jobSeeker);
        return ResponseEntity.ok("Resume uploaded successfully: " + resumeUrl);
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestAttribute("user") User user
    ) {
        System.out.println("📷 Uploading profile pic for: " + user.getUsername());
        System.out.println("➡️ File Name: " + file.getOriginalFilename());

        String profileUrl = userService.uploadProfilePicture(file, user);
        return ResponseEntity.ok("Profile picture uploaded successfully: " + profileUrl);
    }

    @PostMapping("/save-job/{jobId}")
    public ResponseEntity<String> saveJob(@RequestAttribute("user") User user, @PathVariable Long jobId) {
        userService.saveJob(user, jobId);
        return ResponseEntity.ok("Job saved successfully.");
    }

    @PutMapping("/jobseeker/update-profile")
    public ResponseEntity<String> updateProfile(
            @RequestBody JobSeeker updatedInfo,
            @RequestAttribute("user") JobSeeker currentUser
    ) {
        userService.updateJobSeekerProfile(currentUser, updatedInfo);
        return ResponseEntity.ok("Profile updated successfully");
    }

    @DeleteMapping("/unsave-job/{jobId}")
    public ResponseEntity<String> unsaveJob(@RequestAttribute("user") User user, @PathVariable Long jobId) {
        userService.unsaveJob(user, jobId);
        return ResponseEntity.ok("Job removed from saved list.");
    }

    @GetMapping("/saved-jobs")
    public ResponseEntity<List<JobResponseDTO>> getSavedJobs(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(userService.getSavedJobs(user));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(userService.getCurrentUserDto(user));
    }

}
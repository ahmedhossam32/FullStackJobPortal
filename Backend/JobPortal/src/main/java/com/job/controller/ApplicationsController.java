package com.job.controller;

import com.job.dto.request.ApplicationRequestDTO;
import com.job.dto.request.ApplicationStatusUpdateDTO;
import com.job.dto.response.ApplicationResponseDTO;
import com.job.dto.response.ApplicationViewForEmployerDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.enums.ApplicationStatus;
import com.job.enums.Role;
import com.job.service.interfaces.IApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/applications")
public class ApplicationsController {

    private final IApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> applyToJob(
            @RequestBody ApplicationRequestDTO dto,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        log.info("Job seeker {} applying to job id: {}", jobSeeker.getUsername(), dto.getJobId());
        ApplicationResponseDTO response = applicationService.applyToJob(dto, jobSeeker);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/has-applied/{jobId}")
    public ResponseEntity<Boolean> hasAppliedToJob(
            @PathVariable Long jobId,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        boolean applied = applicationService.hasUserAppliedToJob(jobId, jobSeeker);
        return ResponseEntity.ok(applied);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponseDTO>> getMyApplications(
            @RequestAttribute("user") JobSeeker jobSeeker) {
        List<ApplicationResponseDTO> applications = applicationService.getMyApplications(jobSeeker);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(
            @PathVariable Long id,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        ApplicationResponseDTO detail = applicationService.getApplicationById(id, jobSeeker);
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawApplication(
            @PathVariable Long id,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        log.info("Job seeker {} withdrawing application id: {}", jobSeeker.getUsername(), id);
        applicationService.withdrawApplication(id, jobSeeker);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationViewForEmployerDTO>> getApplicationsForJob(
            @PathVariable Long jobId,
            @RequestAttribute("user") User user) {

        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to view applications for job {}", user.getUsername(), jobId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Employer employer = (Employer) user;
        List<ApplicationViewForEmployerDTO> list = applicationService.getApplicationsForJob(jobId, employer);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/employer/{id}")
    public ResponseEntity<ApplicationViewForEmployerDTO> getApplicationForEmployer(
            @PathVariable Long id,
            @RequestAttribute("user") User user) {

        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to view application id: {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Employer employer = (Employer) user;
        ApplicationViewForEmployerDTO dto = applicationService.getApplicationViewForEmployer(id, employer);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody ApplicationStatusUpdateDTO dto,
            @RequestAttribute("user") User user) {

        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to update status for application id: {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }

        Employer employer = (Employer) user;
        log.info("Employer {} updating application id: {} to status: {}", employer.getUsername(), id, dto.getStatus());
        applicationService.updateApplicationStatus(id, dto.getStatus(), employer);
        return ResponseEntity.ok("Application status updated successfully.");
    }

    @GetMapping("/employer")
    public ResponseEntity<List<ApplicationViewForEmployerDTO>> getAllApplicationsForEmployer(
            @RequestAttribute("user") User user,
            @RequestParam(name = "status", required = false) ApplicationStatus status) {

        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to access employer applications", user.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Employer employer = (Employer) user;
        List<ApplicationViewForEmployerDTO> applications = applicationService.getAllApplicationsForEmployer(employer, status);
        return ResponseEntity.ok(applications);
    }
}

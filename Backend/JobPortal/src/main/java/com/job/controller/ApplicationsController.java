package com.job.controller;

import com.job.dto.request.ApplicationRequestDTO;
import com.job.dto.request.ApplicationStatusUpdateDTO;
import com.job.dto.response.ApplicationResponseDTO;
import com.job.dto.response.ApplicationViewForEmployerDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.entity.Employer;
import com.job.entity.JobSeeker;
import com.job.enums.ApplicationStatus;
import com.job.service.interfaces.IApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/applications")
public class ApplicationsController {

    private final IApplicationService applicationService;

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> applyToJob(
            @RequestBody ApplicationRequestDTO dto,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        log.info("Job seeker {} applying to job id: {}", jobSeeker.getUsername(), dto.getJobId());
        ApplicationResponseDTO response = applicationService.applyToJob(dto, jobSeeker);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/has-applied/{jobId}")
    public ResponseEntity<Boolean> hasAppliedToJob(
            @PathVariable Long jobId,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        return ResponseEntity.ok(applicationService.hasUserAppliedToJob(jobId, jobSeeker));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/my")
    public ResponseEntity<PageResponseDTO<ApplicationResponseDTO>> getMyApplications(
            @RequestAttribute("user") JobSeeker jobSeeker,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getMyApplications(jobSeeker, page, size));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(
            @PathVariable Long id,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        return ResponseEntity.ok(applicationService.getApplicationById(id, jobSeeker));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawApplication(
            @PathVariable Long id,
            @RequestAttribute("user") JobSeeker jobSeeker) {
        log.info("Job seeker {} withdrawing application id: {}", jobSeeker.getUsername(), id);
        applicationService.withdrawApplication(id, jobSeeker);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationViewForEmployerDTO>> getApplicationsForJob(
            @PathVariable Long jobId,
            @RequestAttribute("user") Employer employer) {
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, employer));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer/{id}")
    public ResponseEntity<ApplicationViewForEmployerDTO> getApplicationForEmployer(
            @PathVariable Long id,
            @RequestAttribute("user") Employer employer) {
        return ResponseEntity.ok(applicationService.getApplicationViewForEmployer(id, employer));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable Long id,
            @RequestBody ApplicationStatusUpdateDTO dto,
            @RequestAttribute("user") Employer employer) {
        log.info("Employer {} updating application id: {} to status: {}", employer.getUsername(), id, dto.getStatus());
        applicationService.updateApplicationStatus(id, dto.getStatus(), employer);
        return ResponseEntity.ok("Application status updated successfully.");
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer")
    public ResponseEntity<List<ApplicationViewForEmployerDTO>> getAllApplicationsForEmployer(
            @RequestAttribute("user") Employer employer,
            @RequestParam(name = "status", required = false) ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getAllApplicationsForEmployer(employer, status));
    }
}

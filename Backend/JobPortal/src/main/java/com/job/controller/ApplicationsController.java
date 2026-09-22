package com.job.controller;

import com.job.dto.request.ApplicationRequestDTO;
import com.job.dto.request.ApplicationStatusUpdateDTO;
import com.job.dto.response.ApplicationResponseDTO;
import com.job.dto.response.ApplicationViewForEmployerDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.enums.ApplicationStatus;
import com.job.security.SecurityUtils;
import com.job.service.interfaces.IApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    public ResponseEntity<ApplicationResponseDTO> applyToJob(@Valid @RequestBody ApplicationRequestDTO dto) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        log.info("Job seeker id {} applying to job id: {}", jobSeekerId, dto.getJobId());
        ApplicationResponseDTO response = applicationService.applyToJob(dto, jobSeekerId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/has-applied/{jobId}")
    public ResponseEntity<Boolean> hasAppliedToJob(@PathVariable Long jobId) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.hasUserAppliedToJob(jobId, jobSeekerId));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/my")
    public ResponseEntity<PageResponseDTO<ApplicationResponseDTO>> getMyApplications(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.getMyApplications(jobSeekerId, page, size));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(@PathVariable Long id) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.getApplicationById(id, jobSeekerId));
    }

    @PreAuthorize("hasRole('JOB_SEEKER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> withdrawApplication(@PathVariable Long id) {
        Long jobSeekerId = SecurityUtils.getCurrentUserId();
        log.info("Job seeker id {} withdrawing application id: {}", jobSeekerId, id);
        applicationService.withdrawApplication(id, jobSeekerId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationViewForEmployerDTO>> getApplicationsForJob(@PathVariable Long jobId) {
        Long employerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.getApplicationsForJob(jobId, employerId));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer/{id}")
    public ResponseEntity<ApplicationViewForEmployerDTO> getApplicationForEmployer(@PathVariable Long id) {
        Long employerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.getApplicationViewForEmployer(id, employerId));
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateDTO dto) {
        Long employerId = SecurityUtils.getCurrentUserId();
        log.info("Employer id {} updating application id: {} to status: {}", employerId, id, dto.getStatus());
        applicationService.updateApplicationStatus(id, dto.getStatus(), employerId);
        return ResponseEntity.ok("Application status updated successfully.");
    }

    @PreAuthorize("hasRole('EMPLOYER')")
    @GetMapping("/employer")
    public ResponseEntity<List<ApplicationViewForEmployerDTO>> getAllApplicationsForEmployer(
            @RequestParam(name = "status", required = false) ApplicationStatus status) {
        Long employerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(applicationService.getAllApplicationsForEmployer(employerId, status));
    }
}

package com.job.controller;

import com.job.dto.request.JobRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.enums.Role;
import com.job.entity.User;
import com.job.service.interfaces.IJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final IJobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody @Valid JobRequestDTO dto,
                                       @RequestAttribute("user") Employer employer) {
        log.info("Creating job '{}' for employer: {}", dto.getTitle(), employer.getUsername());
        Job createdJob = jobService.createJob(dto, employer);
        log.info("Job created with id: {} by employer: {}", createdJob.getId(), employer.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Job created successfully.", "jobId", createdJob.getId()));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<JobResponseDTO>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getAllJobsSortedByDate(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(@PathVariable Long id) {
        JobResponseDTO job = jobService.getJobById(id);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/search/title")
    public ResponseEntity<PageResponseDTO<JobResponseDTO>> searchByTitle(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchByTitle(keyword, page, size));
    }

    @GetMapping("/search/type")
    public ResponseEntity<PageResponseDTO<JobResponseDTO>> searchByType(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchByType(type, page, size));
    }

    @GetMapping("/search/location")
    public ResponseEntity<PageResponseDTO<JobResponseDTO>> searchByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchByLocation(location, page, size));
    }

    @GetMapping("/search/workmode")
    public ResponseEntity<PageResponseDTO<JobResponseDTO>> searchByWorkMode(
            @RequestParam String workMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.searchByWorkMode(workMode, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(
            @PathVariable Long id,
            @RequestBody @Valid JobRequestDTO jobRequestDTO,
            @RequestAttribute("user") User user) {

        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to update job {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Employer employer = (Employer) user;
        log.info("Updating job id: {} by employer: {}", id, employer.getUsername());
        JobResponseDTO updatedJob = jobService.updateJob(id, jobRequestDTO, employer);
        return ResponseEntity.ok(updatedJob);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(
            @PathVariable Long id,
            @RequestAttribute("user") User user) {

        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to delete job {}", user.getUsername(), id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Employer employer = (Employer) user;
        log.info("Deleting job id: {} by employer: {}", id, employer.getUsername());
        jobService.deleteJob(id, employer);
        return ResponseEntity.ok("Job deleted successfully.");
    }

    @GetMapping("/my")
    public ResponseEntity<List<JobResponseDTO>> getMyJobs(@RequestAttribute("user") User user) {
        if (user.getRole() != Role.EMPLOYER) {
            log.warn("Forbidden: user {} attempted to access employer job list", user.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Employer employer = (Employer) user;
        List<JobResponseDTO> jobs = jobService.getJobsByEmployer(employer);
        return ResponseEntity.ok(jobs);
    }
}

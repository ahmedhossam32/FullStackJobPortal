package com.job.service.impl;

import com.job.dto.request.JobRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.enums.JobType;
import com.job.enums.WorkMode;
import com.job.exception.BadRequestException;
import com.job.exception.ResourceNotFoundException;
import com.job.exception.UnauthorizedException;
import com.job.repository.JobRepository;
import com.job.service.interfaces.IJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements IJobService {

    private final JobRepository jobRepository;

    @Override
    public Job createJob(JobRequestDTO dto, Employer employer) {
        log.info("Creating job '{}' for employer: {}", dto.getTitle(), employer.getUsername());
        Job job = new Job();

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType() != null ? dto.getType() : JobType.FULL_TIME); // Default type
        job.setWorkMode(dto.getWorkMode() != null ? dto.getWorkMode() : WorkMode.HYBRID); // Default work mode
        job.setResponsibilities(dto.getResponsibilities());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setScreeningQuestions(dto.getScreeningQuestions()); // Optional field
        job.setPostedAt(LocalDateTime.now());
        job.setEmployer(employer);

        return jobRepository.save(job);
    }

    @Override
    public List<JobResponseDTO> getAllJobsSortedByDate() {
        return jobRepository.findAllByOrderByPostedAtDesc().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<JobResponseDTO> searchByTitle(String keyword) {
        return jobRepository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<JobResponseDTO> searchByType(String type) {
        try {
            JobType jobType = JobType.valueOf(type.toUpperCase());
            return jobRepository.findByType(jobType).stream()
                    .map(this::mapToDTO)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid job type provided: {}", type);
            throw new BadRequestException("Invalid job type: " + type);
        }
    }

    @Override
    public List<JobResponseDTO> searchByLocation(String location) {
        return jobRepository.findByLocationContainingIgnoreCase(location).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<JobResponseDTO> searchByWorkMode(String workMode) {
        try {
            WorkMode mode = WorkMode.valueOf(workMode.toUpperCase());
            return jobRepository.findByWorkMode(mode).stream()
                    .map(this::mapToDTO)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid work mode provided: {}", workMode);
            throw new BadRequestException("Invalid work mode: " + workMode);
        }
    }

    @Override
    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return mapToDTO(job);
    }

    @Override
    public JobResponseDTO updateJob(Long id, JobRequestDTO dto, Employer employer) {
        log.info("Updating job id: {} by employer: {}", id, employer.getUsername());
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You are not authorized to update this job");
        }

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setType(dto.getType());
        job.setWorkMode(dto.getWorkMode());
        job.setRequiredSkills(dto.getRequiredSkills());
        job.setResponsibilities(dto.getResponsibilities());

        Job updated = jobRepository.save(job);
        return mapToDTO(updated);
    }

    @Override
    public void deleteJob(Long jobId, Employer employer) {
        log.info("Deleting job id: {} by employer: {}", jobId, employer.getUsername());
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this job");
        }

        jobRepository.delete(job);
    }

    @Override
    public List<JobResponseDTO> getJobsByEmployer(Employer employer) {
        return jobRepository.findByEmployer(employer).stream()
                .map(this::mapToDTO)
                .toList();
    }

    private JobResponseDTO mapToDTO(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setLocation(job.getLocation());
        dto.setPostedAt(job.getPostedAt());
        dto.setCompanyName(job.getEmployer().getCompanyName());
        dto.setType(job.getType());
        dto.setWorkMode(job.getWorkMode());
        dto.setRequiredSkills(job.getRequiredSkills());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setProfilePicture(job.getEmployer().getProfilePictureFileName());
        dto.setScreeningQuestions(job.getScreeningQuestions());
        return dto;
    }
}

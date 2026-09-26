package com.job.service.impl;

import com.job.dto.request.JobRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.entity.Employer;
import com.job.entity.Job;
import com.job.enums.JobType;
import com.job.enums.WorkMode;
import com.job.exception.BadRequestException;
import com.job.exception.ForbiddenException;
import com.job.exception.ResourceNotFoundException;
import com.job.mapper.JobMapper;
import com.job.repository.EmployerRepository;
import com.job.repository.JobRepository;
import com.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponseDTO createJob(JobRequestDTO dto, Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        log.info("Creating job '{}' for employer: {}", dto.title(), employer.getUsername());
        Job job = jobMapper.toEntity(dto);
        job.setPostedAt(LocalDateTime.now());
        job.setEmployer(employer);

        return jobMapper.toResponseDTO(jobRepository.save(job));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<JobResponseDTO> getAllJobsSortedByDate(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("postedAt").descending());
        return toPageResponse(jobRepository.findAllWithEmployer(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<JobResponseDTO> searchByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return toPageResponse(jobRepository.findByTitleContainingIgnoreCaseWithEmployer(keyword, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<JobResponseDTO> searchByType(String type, int page, int size) {
        try {
            JobType jobType = JobType.valueOf(type.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            return toPageResponse(jobRepository.findByTypeWithEmployer(jobType, pageable));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid job type provided: {}", type);
            throw new BadRequestException("Invalid job type: " + type);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<JobResponseDTO> searchByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return toPageResponse(jobRepository.findByLocationContainingIgnoreCaseWithEmployer(location, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<JobResponseDTO> searchByWorkMode(String workMode, int page, int size) {
        try {
            WorkMode mode = WorkMode.valueOf(workMode.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            return toPageResponse(jobRepository.findByWorkModeWithEmployer(mode, pageable));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid work mode provided: {}", workMode);
            throw new BadRequestException("Invalid work mode: " + workMode);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponseDTO getJobById(Long id) {
        Job job = jobRepository.findByIdWithEmployer(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return jobMapper.toResponseDTO(job);
    }

    @Override
    @Transactional
    public JobResponseDTO updateJob(Long id, JobRequestDTO dto, Long employerId) {
        log.info("Updating job id: {} by employer id: {}", id, employerId);
        Job job = jobRepository.findByIdWithEmployer(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("You are not authorized to update this job");
        }

        jobMapper.updateJobFromDto(dto, job);

        Job updated = jobRepository.save(job);
        return jobMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, Long employerId) {
        log.info("Deleting job id: {} by employer id: {}", jobId, employerId);
        Job job = jobRepository.findByIdWithEmployer(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("You are not authorized to delete this job");
        }

        jobRepository.delete(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> getJobsByEmployer(Long employerId) {
        return jobRepository.findByEmployerIdWithEmployer(employerId).stream()
                .map(jobMapper::toResponseDTO)
                .toList();
    }

    private PageResponseDTO<JobResponseDTO> toPageResponse(Page<Job> page) {
        List<JobResponseDTO> content = page.getContent().stream()
                .map(jobMapper::toResponseDTO)
                .toList();
        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isLast()
        );
    }
}

package com.job.service.impl;

import com.job.dto.request.ApplicationRequestDTO;
import com.job.dto.response.ApplicationResponseDTO;
import com.job.dto.response.ApplicationViewForEmployerDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.entity.Application;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.enums.ApplicationStatus;
import com.job.exception.BadRequestException;
import com.job.exception.DuplicateResourceException;
import com.job.exception.ForbiddenException;
import com.job.exception.ResourceNotFoundException;
import com.job.event.ApplicationStatusChangedEvent;
import com.job.event.ApplicationSubmittedEvent;
import com.job.mapper.ApplicationMapper;
import com.job.repository.ApplicationRepository;
import com.job.repository.JobRepository;
import com.job.repository.JobSeekerRepository;
import com.job.service.ApplicationService;
import com.job.util.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public ApplicationResponseDTO applyToJob(ApplicationRequestDTO dto, Long jobSeekerId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));
        if (jobSeeker.getResumeUrl() == null || jobSeeker.getResumeUrl().isBlank()) {
            throw new BadRequestException("You must upload a resume before applying to a job.");
        }

        Job job = jobRepository.findById(dto.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        boolean alreadyApplied = applicationRepository.existsByJobAndJobSeeker(job, jobSeeker);
        if (alreadyApplied) {
            throw new DuplicateResourceException("You have already applied for this job");
        }

        List<String> screeningQs = job.getScreeningQuestions();
        if (screeningQs != null && !screeningQs.isEmpty()) {
            if (dto.screeningAnswers() == null || dto.screeningAnswers().size() != screeningQs.size()) {
                throw new BadRequestException("You must answer all required screening questions.");
            }
        }

        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(jobSeeker);
        application.setResumeUrl(jobSeeker.getResumeUrl());
        application.setStatus(ApplicationStatus.PENDING);

        applicationRepository.save(application);

        eventPublisher.publishEvent(new ApplicationSubmittedEvent(
                jobSeeker.getEmail(),
                jobSeeker.getName(),
                job.getTitle(),
                job.getEmployer().getCompanyName()
        ));

        return applicationMapper.toResponseDTO(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationViewForEmployerDTO> getAllApplicationsForEmployer(Long employerId, ApplicationStatus status) {
        List<Application> applications;

        if (status == null) {
            applications = applicationRepository.findByJob_EmployerId(employerId);
        } else {
            applications = applicationRepository.findByJob_EmployerIdAndStatus(employerId, status);
        }

        return applications.stream()
                .map(applicationMapper::toEmployerView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ApplicationResponseDTO> getMyApplications(Long jobSeekerId, int page, int size) {
        return PageMapper.toPageResponse(applicationRepository.findByJobSeekerId(jobSeekerId, PageRequest.of(page, size))
                .map(applicationMapper::toResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDTO getApplicationById(Long id, Long requesterId) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!app.getJobSeeker().getId().equals(requesterId)) {
            throw new ForbiddenException("Unauthorized access to application");
        }

        return applicationMapper.toResponseDTO(app);
    }

    @Override
    @Transactional
    public void withdrawApplication(Long id, Long requesterId) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!app.getJobSeeker().getId().equals(requesterId)) {
            throw new ForbiddenException("Unauthorized to withdraw this application");
        }

        applicationRepository.delete(app);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationViewForEmployerDTO> getApplicationsForJob(Long jobId, Long employerId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!job.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized to view applications for this job");
        }

        List<Application> applications = applicationRepository.findByJob(job);

        return applications.stream()
                .map(applicationMapper::toEmployerView)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationViewForEmployerDTO getApplicationViewForEmployer(Long id, Long employerId) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!app.getJob().getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized to view this application");
        }

        return applicationMapper.toEmployerView(app);
    }

    @Override
    @Transactional
    public void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, Long employerId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!app.getJob().getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("Unauthorized to update this application");
        }

        app.setStatus(newStatus);
        applicationRepository.save(app);

        JobSeeker jobSeeker = app.getJobSeeker();
        eventPublisher.publishEvent(new ApplicationStatusChangedEvent(
                app.getId(),
                jobSeeker.getId(),
                jobSeeker.getEmail(),
                jobSeeker.getName(),
                app.getJob().getTitle(),
                app.getJob().getEmployer().getCompanyName(),
                newStatus
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserAppliedToJob(Long jobId, Long jobSeekerId) {
        return applicationRepository.existsByJobIdAndJobSeekerId(jobId, jobSeekerId);
    }
}

package com.job.service.impl;

import com.job.dto.response.JobResponseDTO;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.entity.User;
import com.job.exception.BadRequestException;
import com.job.exception.DuplicateResourceException;
import com.job.exception.ResourceNotFoundException;
import com.job.repository.JobRepository;
import com.job.repository.JobSeekerRepository;
import com.job.service.interfaces.ISavedJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements ISavedJobService {

    private final JobSeekerRepository jobSeekerRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public void saveJob(User user, Long jobId) {
        if (!(user instanceof JobSeeker)) {
            throw new BadRequestException("Only job seekers can save jobs.");
        }

        Long jobSeekerId = user.getId();
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (jobSeeker.getSavedJobs().contains(job)) {
            throw new DuplicateResourceException("You already saved this job.");
        }

        log.info("Job seeker {} saving job id: {}", user.getUsername(), jobId);
        jobSeeker.getSavedJobs().add(job);
        jobSeekerRepository.save(jobSeeker);
    }

    @Override
    @Transactional
    public void unsaveJob(User user, Long jobId) {
        if (!(user instanceof JobSeeker)) {
            throw new BadRequestException("Only job seekers can unsave jobs.");
        }

        Long jobSeekerId = user.getId();
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!jobSeeker.getSavedJobs().contains(job)) {
            throw new ResourceNotFoundException("This job is not in your saved list.");
        }

        log.info("Job seeker {} unsaving job id: {}", user.getUsername(), jobId);
        jobSeeker.getSavedJobs().remove(job);
        jobSeekerRepository.save(jobSeeker);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> getSavedJobs(User user) {
        if (!(user instanceof JobSeeker)) {
            throw new BadRequestException("Only job seekers can view saved jobs.");
        }

        Long jobSeekerId = user.getId();
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));

        return jobSeeker.getSavedJobs().stream()
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
        dto.setProfilePicture(job.getEmployer().getProfilePictureUrl());
        dto.setType(job.getType());
        dto.setWorkMode(job.getWorkMode());
        dto.setResponsibilities(job.getResponsibilities());
        dto.setRequiredSkills(job.getRequiredSkills());
        return dto;
    }
}

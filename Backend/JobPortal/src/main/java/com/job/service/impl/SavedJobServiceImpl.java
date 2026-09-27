package com.job.service.impl;

import com.job.dto.response.JobResponseDTO;
import com.job.entity.Job;
import com.job.entity.JobSeeker;
import com.job.exception.DuplicateResourceException;
import com.job.exception.ResourceNotFoundException;
import com.job.mapper.JobMapper;
import com.job.repository.JobRepository;
import com.job.repository.JobSeekerRepository;
import com.job.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedJobServiceImpl implements SavedJobService {

    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public void saveJob(Long jobSeekerId, Long jobId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (jobSeeker.getSavedJobs().contains(job)) {
            throw new DuplicateResourceException("You already saved this job.");
        }

        jobSeeker.getSavedJobs().add(job);
    }

    @Override
    @Transactional
    public void unsaveJob(Long jobSeekerId, Long jobId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (!jobSeeker.getSavedJobs().contains(job)) {
            throw new ResourceNotFoundException("This job is not in your saved list.");
        }

        jobSeeker.getSavedJobs().remove(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDTO> getSavedJobs(Long jobSeekerId) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker not found"));

        return jobSeeker.getSavedJobs().stream()
                .map(jobMapper::toResponseDTO)
                .toList();
    }
}
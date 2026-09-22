package com.job.service.interfaces;

import com.job.dto.response.JobResponseDTO;

import java.util.List;

public interface ISavedJobService {
    void saveJob(Long jobSeekerId, Long jobId);
    void unsaveJob(Long jobSeekerId, Long jobId);
    List<JobResponseDTO> getSavedJobs(Long jobSeekerId);
}

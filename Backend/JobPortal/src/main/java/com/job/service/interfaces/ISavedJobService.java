package com.job.service.interfaces;

import com.job.dto.response.JobResponseDTO;
import com.job.entity.User;

import java.util.List;

public interface ISavedJobService {
    void saveJob(User user, Long jobId);
    void unsaveJob(User user, Long jobId);
    List<JobResponseDTO> getSavedJobs(User user);
}
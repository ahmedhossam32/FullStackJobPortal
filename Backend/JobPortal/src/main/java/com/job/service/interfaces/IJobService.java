package com.job.service.interfaces;

import com.job.dto.request.JobRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.entity.Employer;
import com.job.entity.Job;

import java.util.List;

public interface IJobService {
    Job createJob(JobRequestDTO dto, Employer employer);
    List<JobResponseDTO> getAllJobsSortedByDate();
    List<JobResponseDTO> searchByTitle(String keyword);
    List<JobResponseDTO> searchByType(String type);
    List<JobResponseDTO> searchByLocation(String location);
    List<JobResponseDTO> searchByWorkMode(String workMode);
    JobResponseDTO getJobById(Long id);
    JobResponseDTO updateJob(Long id, JobRequestDTO dto, Employer employer);
    void deleteJob(Long jobId, Employer employer);
    List<JobResponseDTO> getJobsByEmployer(Employer employer);
}

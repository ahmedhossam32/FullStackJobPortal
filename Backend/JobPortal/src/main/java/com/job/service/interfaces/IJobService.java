package com.job.service.interfaces;

import com.job.dto.request.JobRequestDTO;
import com.job.dto.response.JobResponseDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.entity.Job;

import java.util.List;

public interface IJobService {
    Job createJob(JobRequestDTO dto, Long employerId);
    PageResponseDTO<JobResponseDTO> getAllJobsSortedByDate(int page, int size);
    PageResponseDTO<JobResponseDTO> searchByTitle(String keyword, int page, int size);
    PageResponseDTO<JobResponseDTO> searchByType(String type, int page, int size);
    PageResponseDTO<JobResponseDTO> searchByLocation(String location, int page, int size);
    PageResponseDTO<JobResponseDTO> searchByWorkMode(String workMode, int page, int size);
    JobResponseDTO getJobById(Long id);
    JobResponseDTO updateJob(Long id, JobRequestDTO dto, Long employerId);
    void deleteJob(Long jobId, Long employerId);
    List<JobResponseDTO> getJobsByEmployer(Long employerId);
}

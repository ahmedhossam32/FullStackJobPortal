package com.job.service.interfaces;

import com.job.dto.request.ApplicationRequestDTO;
import com.job.dto.response.ApplicationResponseDTO;
import com.job.dto.response.ApplicationViewForEmployerDTO;
import com.job.dto.response.PageResponseDTO;
import com.job.enums.ApplicationStatus;

import java.util.List;

public interface IApplicationService {
    ApplicationResponseDTO applyToJob(ApplicationRequestDTO dto, Long jobSeekerId);
    List<ApplicationViewForEmployerDTO> getAllApplicationsForEmployer(Long employerId, ApplicationStatus status);
    PageResponseDTO<ApplicationResponseDTO> getMyApplications(Long jobSeekerId, int page, int size);
    ApplicationResponseDTO getApplicationById(Long id, Long requesterId);
    void withdrawApplication(Long id, Long requesterId);
    List<ApplicationViewForEmployerDTO> getApplicationsForJob(Long jobId, Long employerId);
    ApplicationViewForEmployerDTO getApplicationViewForEmployer(Long id, Long employerId);
    void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, Long employerId);
    boolean hasUserAppliedToJob(Long jobId, Long jobSeekerId);
}
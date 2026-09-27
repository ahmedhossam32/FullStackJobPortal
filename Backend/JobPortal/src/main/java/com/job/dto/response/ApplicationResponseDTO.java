package com.job.dto.response;

import com.job.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponseDTO(
        Long applicationId,
        Long jobId,
        String username,
        String resumeUrl,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        String jobTitle,
        String jobDescription,
        String jobType,
        String workMode,
        String location,
        String companyName,
        String companyLogoUrl
) {
}

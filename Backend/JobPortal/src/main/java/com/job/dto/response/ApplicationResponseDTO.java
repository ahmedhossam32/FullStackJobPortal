package com.job.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.job.enums.ApplicationStatus;

import java.time.LocalDateTime;

// Keeps the alphabetical JSON key order this DTO had as a class. Records serialize in component
// order, and @JsonPropertyOrder(alphabetic = true) does not override that, so the names are listed.
@JsonPropertyOrder({"applicationId", "appliedAt", "companyLogoUrl", "companyName", "jobDescription", "jobId", "jobTitle", "jobType", "location", "resumeUrl", "status", "username", "workMode"})
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

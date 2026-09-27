package com.job.dto.response;

import com.job.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApplicationViewForEmployerDTO(
        long id,
        String applicantUsername,
        String applicantEmail,
        LocalDate applicantDOB,
        String applicantName,
        String applicantProfilePicture,
        String resumeUrl,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        String jobTitle
) {
}

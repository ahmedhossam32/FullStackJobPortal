package com.job.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.job.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Keeps the alphabetical JSON key order this DTO had as a class. Records serialize in component
// order, and @JsonPropertyOrder(alphabetic = true) does not override that, so the names are listed.
@JsonPropertyOrder({"applicantDOB", "applicantEmail", "applicantName", "applicantProfilePicture", "applicantUsername", "appliedAt", "id", "jobTitle", "resumeUrl", "status"})
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

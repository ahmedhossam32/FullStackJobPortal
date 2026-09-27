package com.job.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;

// Keeps the alphabetical JSON key order this DTO had as a class. Records serialize in component
// order, and @JsonPropertyOrder(alphabetic = true) does not override that, so the names are listed.
@JsonPropertyOrder({"applicationId", "companyLogoUrl", "createdAt", "id", "message", "seen"})
public record NotificationDTO(
        Long id,
        String message,
        LocalDateTime createdAt,
        boolean seen,
        String companyLogoUrl,
        Long applicationId
) {
}

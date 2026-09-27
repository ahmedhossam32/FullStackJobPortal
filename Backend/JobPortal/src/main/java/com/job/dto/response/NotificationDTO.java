package com.job.dto.response;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String message,
        LocalDateTime createdAt,
        boolean seen,
        String companyLogoUrl,
        Long applicationId
) {
}

package com.job.dto.request;

import com.job.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateDTO(

        @NotNull(message = "Application status is required")
        ApplicationStatus status
) {
}

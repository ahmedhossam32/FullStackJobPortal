package com.job.event;

import com.job.enums.ApplicationStatus;

public record ApplicationStatusChangedEvent(
        Long applicationId,
        Long jobSeekerId,
        String seekerEmail,
        String seekerName,
        String jobTitle,
        String companyName,
        ApplicationStatus newStatus
) {
}

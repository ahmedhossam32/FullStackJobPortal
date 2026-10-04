package com.job.event;

public record ApplicationSubmittedEvent(
        String seekerEmail,
        String seekerName,
        String jobTitle,
        String companyName
) {
}

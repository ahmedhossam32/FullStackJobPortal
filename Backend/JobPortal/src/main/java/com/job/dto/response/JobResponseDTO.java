package com.job.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.job.enums.JobType;
import com.job.enums.WorkMode;

import java.time.LocalDateTime;
import java.util.List;

// Keeps the alphabetical JSON key order this DTO had as a class. Records serialize in component
// order, and @JsonPropertyOrder(alphabetic = true) does not override that, so the names are listed.
@JsonPropertyOrder({"companyName", "description", "employerId", "id", "location", "postedAt", "profilePicture", "requiredSkills", "responsibilities", "screeningQuestions", "title", "type", "workMode"})
public record JobResponseDTO(
        Long id,
        String title,
        String description,
        String location,
        LocalDateTime postedAt,
        String profilePicture,
        String companyName,
        JobType type,
        WorkMode workMode,
        List<String> responsibilities,
        List<String> requiredSkills,
        List<String> screeningQuestions,
        Long employerId
) {
}

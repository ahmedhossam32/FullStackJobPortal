package com.job.dto.response;

import com.job.enums.JobType;
import com.job.enums.WorkMode;

import java.time.LocalDateTime;
import java.util.List;

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

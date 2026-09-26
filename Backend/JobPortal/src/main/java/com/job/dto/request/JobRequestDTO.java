package com.job.dto.request;

import com.job.enums.JobType;
import com.job.enums.WorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record JobRequestDTO(

        @NotBlank(message = "Job title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Job description is required")
        String description,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Job type is required")
        JobType type,

        @NotNull(message = "Work mode is required")
        WorkMode workMode,

        @Size(max = 25, message = "Responsibilities must not exceed 25 items")
        List<@Size(max = 500, message = "Each responsibility must not exceed 500 characters") String> responsibilities,

        @Size(max = 25, message = "Required skills must not exceed 25 items")
        List<@Size(max = 500, message = "Each required skill must not exceed 500 characters") String> requiredSkills,

        @Size(max = 25, message = "Screening questions must not exceed 25 items")
        List<@Size(max = 500, message = "Each screening question must not exceed 500 characters") String> screeningQuestions
) {
}

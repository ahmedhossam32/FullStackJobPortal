package com.job.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ApplicationRequestDTO(

        @NotNull(message = "Job ID is required")
        Long jobId,

        List<@NotBlank(message = "Screening answer must not be blank") String> screeningAnswers
) {
}

package com.job.dto;

import com.job.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class ApplicationResponseDTO {
    private Long applicationId;

    private String username;
    private String resumeUrl;

    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    private String jobTitle;
    private String jobDescription;
    private String jobType;        // e.g., FULL_TIME
    private String workMode;       // e.g., REMOTE
    private String location;

    private String companyName;
    private String companyLogoUrl;
    private Map<String, String> screeningAnswers;
}

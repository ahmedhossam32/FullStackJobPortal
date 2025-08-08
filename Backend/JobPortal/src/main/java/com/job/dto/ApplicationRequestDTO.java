package com.job.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApplicationRequestDTO {
    private Long jobId;
    private String resumeUrl;
    private List<String> screeningAnswers;
}


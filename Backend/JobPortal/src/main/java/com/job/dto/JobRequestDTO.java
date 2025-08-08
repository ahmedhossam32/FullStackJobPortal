package com.job.dto;

import com.job.enums.JobType;
import com.job.enums.WorkMode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JobRequestDTO {
    private String title;
    private String description;
    private String location;
    private JobType type;
    private WorkMode workMode;

    private List<String> responsibilities;
    private List<String> requiredSkills;
    private List<String> screeningQuestions;

}

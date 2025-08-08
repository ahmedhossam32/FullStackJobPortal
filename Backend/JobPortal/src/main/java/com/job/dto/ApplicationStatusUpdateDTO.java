package com.job.dto;

import com.job.enums.ApplicationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusUpdateDTO {
    private ApplicationStatus status;
}

package com.job.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private boolean seen;
    private String companyLogoUrl;
}

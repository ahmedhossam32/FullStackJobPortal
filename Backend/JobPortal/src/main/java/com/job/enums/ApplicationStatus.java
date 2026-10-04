package com.job.enums;

public enum ApplicationStatus {
    PENDING,
    REVIEWED,
    INTERVIEW,
    OFFERED,
    REJECTED,
    WITHDRAWN;

    public String displayLabel() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}

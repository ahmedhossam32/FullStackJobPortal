package com.job.designpatterns.strategy;

import com.job.entity.Job;
import com.job.enums.JobType;

import java.util.List;
public class SortByTypeStrategy implements JobSortStrategy {

    private final JobType type;

    public SortByTypeStrategy(String typeStr) {
        String normalized = typeStr.trim().toUpperCase().replaceAll("[\\s_-]", "");

        this.type = switch (normalized) {
            case "FULLTIME"     -> JobType.FULL_TIME;
            case "PARTTIME"     -> JobType.PART_TIME;
            case "CONTRACT"     -> JobType.CONTRACT;
            case "INTERNSHIP"   -> JobType.INTERNSHIP;
            default -> throw new IllegalArgumentException("Invalid job type: " + typeStr);
        };
    }



    @Override
    public List<Job> sort(List<Job> jobs) {
        return jobs.stream()
                .filter(job -> job.getType() != null && job.getType().equals(type))
                .toList();
    }
}


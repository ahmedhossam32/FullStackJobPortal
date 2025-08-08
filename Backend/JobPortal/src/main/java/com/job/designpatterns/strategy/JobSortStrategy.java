package com.job.designpatterns.strategy;

import com.job.entity.Job;
import java.util.List;

public interface JobSortStrategy {
    List<Job> sort(List<Job> jobs);
}

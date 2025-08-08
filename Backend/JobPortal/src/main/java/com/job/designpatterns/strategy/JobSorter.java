package com.job.designpatterns.strategy;

import com.job.entity.Job;

import java.util.List;

public class JobSorter {

    private JobSortStrategy strategy;

    public void setStrategy(JobSortStrategy strategy) {
        this.strategy = strategy;
    }

    public List<Job> sortJobs(List<Job> jobs) {
        if (strategy == null) {
            throw new IllegalStateException("Sort strategy is not set.");
        }
        return strategy.sort(jobs);
    }
}

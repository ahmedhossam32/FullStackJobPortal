package com.job.designpatterns.strategy;

import com.job.entity.Job;

import java.util.Comparator;
import java.util.List;

public class SortByDateStrategy implements JobSortStrategy {

    @Override
    public List<Job> sort(List<Job> jobs) {
        return jobs.stream()
                .sorted(Comparator.comparing(Job::getPostedAt).reversed())
                .toList();
    }
}

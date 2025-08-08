package com.job.designpatterns.strategy;

import com.job.entity.Job;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
public class SortByTitleStrategy implements JobSortStrategy {

    private final String keyword;

    public SortByTitleStrategy(String keyword) {
        this.keyword = keyword.toLowerCase(Locale.ROOT);
    }

    @Override
    public List<Job> sort(List<Job> jobs) {
        return jobs.stream()
                .filter(job -> keyword.isBlank() || (
                        job.getTitle() != null &&
                                job.getTitle().toLowerCase().contains(keyword)
                ))
                .sorted(Comparator.comparing(Job::getTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

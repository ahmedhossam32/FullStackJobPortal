package com.job.designpatterns.strategy;

import com.job.enums.SortType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobSortContext {

    public JobSortStrategy getStrategy(SortType sortType, String keyword) {
        String normalizedKeyword = keyword != null ? keyword.trim() : "";

        return switch (sortType) {
            case TITLE -> new SortByTitleStrategy(normalizedKeyword);
            case TYPE -> {
                try {
                    yield new SortByTypeStrategy(normalizedKeyword);
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ Invalid JobType keyword: " + normalizedKeyword);
                    yield jobs -> List.of(); // return empty list on invalid input
                }
            }
            case DATE -> new SortByDateStrategy();
        };
    }
}

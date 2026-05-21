package com.job.designpatterns.strategy;

import com.job.enums.SortType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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
                    log.warn("Invalid JobType keyword: {}", normalizedKeyword);
                    yield jobs -> List.of(); // return empty list on invalid input
                }
            }
            case DATE -> new SortByDateStrategy();
        };
    }
}

package com.job.util;

import com.job.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;

public final class PageMapper {

    private PageMapper() {
    }

    // Map entities to DTOs with page.map(...) first; Page.map keeps the paging metadata.
    public static <T> PageResponseDTO<T> toPageResponse(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isLast()
        );
    }
}

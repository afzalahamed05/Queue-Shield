package com.queueshield.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable, hand-controlled shape for paginated responses. Returning Spring Data's {@code Page}
 * straight from a controller works but couples the API contract to Spring Data's internal
 * serialization and triggers a Jackson support warning; wrapping it keeps the JSON contract ours.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

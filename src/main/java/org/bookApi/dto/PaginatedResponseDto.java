package org.bookApi.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Paginated response wrapper")
public record PaginatedResponseDto<T>(
        @Schema(description = "List of items in the current page")
        List<T> list,

        @Schema(description = "Total number of pages available", example = "5")
        int totalPages
) {
}

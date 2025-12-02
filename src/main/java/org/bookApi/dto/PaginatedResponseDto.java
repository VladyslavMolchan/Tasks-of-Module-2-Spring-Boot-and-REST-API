package org.bookApi.dto;

import java.util.List;

public record PaginatedResponseDto<T>(
        List<T> list,
        int totalPages
) {}
package org.bookApi.dto;

public record UploadResponseDto(
        int successCount,
        int failedCount
) {}

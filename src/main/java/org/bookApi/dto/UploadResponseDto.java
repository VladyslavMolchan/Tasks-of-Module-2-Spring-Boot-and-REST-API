package org.bookApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response DTO for file upload results")
public record UploadResponseDto(
        @Schema(description = "Number of successfully processed items", example = "10")
        int successCount,

        @Schema(description = "Number of failed items", example = "2")
        int failedCount
) {
}

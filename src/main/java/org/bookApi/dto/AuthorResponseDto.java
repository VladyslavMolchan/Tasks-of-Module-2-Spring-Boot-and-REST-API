package org.bookApi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO representing an author in responses")
public record AuthorResponseDto(
        @Schema(description = "Unique identifier of the author", example = "1")
        Long id,

        @Schema(description = "Name of the author", example = "J.K. Rowling")
        String name
) {
}

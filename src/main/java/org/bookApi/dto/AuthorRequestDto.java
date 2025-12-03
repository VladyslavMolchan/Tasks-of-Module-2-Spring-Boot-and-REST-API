package org.bookApi.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for creating or updating an author")
public record AuthorRequestDto(
        @NotBlank
        @Schema(description = "Name of the author", example = "J.K. Rowling", required = true)
        String name
) {
}

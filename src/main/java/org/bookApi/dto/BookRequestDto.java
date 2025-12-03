package org.bookApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "DTO for creating or updating a book")
public record BookRequestDto(
        @Schema(description = "Title of the book", example = "Harry Potter and the Philosopher's Stone", required = true)
        @NotBlank
        String title,

        @Schema(description = "ID of the author", example = "1", required = true)
        @NotNull
        Long authorId,

        @Schema(description = "Year the book was published", example = "1997")
        Integer yearPublished,

        @Schema(description = "Genres of the book", example = "[\"Fantasy\", \"Adventure\"]")
        List<String> genres
) {
}

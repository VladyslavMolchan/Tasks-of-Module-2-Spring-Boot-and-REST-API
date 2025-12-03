package org.bookApi.dto;

import org.bookApi.dto.AuthorResponseDto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO representing a book in responses")
public record BookResponseDto(
        @Schema(description = "Unique identifier of the book", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "Harry Potter and the Philosopher's Stone")
        String title,

        @Schema(description = "Author of the book")
        AuthorResponseDto author,

        @Schema(description = "Year the book was published", example = "1997")
        Integer yearPublished,

        @Schema(description = "Genres of the book", example = "[\"Fantasy\", \"Adventure\"]")
        List<String> genres
) {
}

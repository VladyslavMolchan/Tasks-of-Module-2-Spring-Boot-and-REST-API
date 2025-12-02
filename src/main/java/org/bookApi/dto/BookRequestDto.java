package org.bookApi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookRequestDto(
        @NotBlank String title,
        @NotNull Long authorId,
        Integer yearPublished,
        List<String> genres
) {}
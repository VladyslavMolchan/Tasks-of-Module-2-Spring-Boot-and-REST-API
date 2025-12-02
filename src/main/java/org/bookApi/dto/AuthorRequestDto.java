package org.bookApi.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequestDto(
        @NotBlank
        String name
) {}

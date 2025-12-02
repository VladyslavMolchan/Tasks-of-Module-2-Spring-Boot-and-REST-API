package org.bookApi.dto;

import org.bookApi.dto.AuthorResponseDto;

import java.util.List;

public record BookResponseDto(
        Long id,
        String title,
        AuthorResponseDto author,
        Integer yearPublished,
        List<String> genres
) {}
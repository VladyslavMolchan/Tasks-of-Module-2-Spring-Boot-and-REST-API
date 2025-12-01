package org.bookApi.mapper;

import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.entity.Author;

public class AuthorMapper {

    public static AuthorResponseDto toDto(Author author) {
        if (author == null) return null;

        return AuthorResponseDto.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }

    public static Author fromRequestDto(AuthorRequestDto dto) {
        return Author.builder()
                .name(dto.getName())
                .build();
    }

    public static void updateEntity(Author author, AuthorRequestDto dto) {
        author.setName(dto.getName());
    }
}

package org.bookApi.mapper;

import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.entity.Author;

public class AuthorMapper {
    public static AuthorResponseDto toDto(Author author) {
        return new AuthorResponseDto(author.getId(), author.getName());
    }
}

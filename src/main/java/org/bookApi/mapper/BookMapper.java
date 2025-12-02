package org.bookApi.mapper;


import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.dto.BookResponseDto;
import org.bookApi.entity.Book;

import java.util.List;

public class BookMapper {
    public static BookResponseDto toDto(Book book) {
        return new BookResponseDto(
                book.getId(),
                book.getTitle(),
                new AuthorResponseDto(book.getAuthor().getId(), book.getAuthor().getName()),
                book.getYearPublished(),
                book.getGenres() == null ? List.of() : book.getGenres()
        );
    }
}

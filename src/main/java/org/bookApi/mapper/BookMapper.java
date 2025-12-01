package org.bookApi.mapper;


import org.bookApi.dto.BookResponseDto;
import org.bookApi.entity.Book;

import java.util.List;

public class BookMapper {

    public static BookResponseDto toDto(Book book) {

        if (book == null) return null;

        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .yearPublished(book.getYearPublished())
                .genres(book.getGenres() == null ? List.of() : book.getGenres())
                .author(BookResponseDto.AuthorDto.builder()
                        .id(book.getAuthor().getId())
                        .name(book.getAuthor().getName())
                        .build())
                .build();
    }
}

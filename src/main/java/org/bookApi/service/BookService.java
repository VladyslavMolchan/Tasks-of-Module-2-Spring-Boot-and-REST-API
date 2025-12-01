package org.bookApi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.bookApi.dto.BookRequestDto;
import org.bookApi.dto.BookResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.bookApi.dto.UploadResponseDto;
import org.bookApi.entity.Author;
import org.bookApi.entity.Book;
import org.bookApi.exception.ResourceNotFoundException;
import org.bookApi.mapper.BookMapper;
import org.bookApi.repository.AuthorRepository;
import org.bookApi.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BookResponseDto create(BookRequestDto dto) {
        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Book book = Book.builder()
                .title(dto.getTitle())
                .author(author)
                .yearPublished(dto.getYearPublished())
                .genres(dto.getGenres() == null ? new ArrayList<>() : dto.getGenres())
                .build();

        return BookMapper.toDto(bookRepository.save(book));
    }

    public BookResponseDto update(Long id, BookRequestDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        book.setTitle(dto.getTitle());
        book.setAuthor(author);
        book.setYearPublished(dto.getYearPublished());
        book.setGenres(dto.getGenres() == null ? new ArrayList<>() : dto.getGenres());

        return BookMapper.toDto(bookRepository.save(book));
    }

    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        bookRepository.delete(book);
    }

    public BookResponseDto getById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        return BookMapper.toDto(book);
    }

    public PaginatedResponseDto<BookResponseDto> getList(
            Long authorId,
            String title,
            Integer yearPublished,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("title"));

        Page<Book> books;

        if (authorId != null) {
            Author author = authorRepository.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

            if (title != null && yearPublished != null) {
                books = bookRepository.findByAuthorAndTitleContainingIgnoreCaseAndYearPublished(
                        author, title, yearPublished, pageable
                );
            } else if (title != null) {
                books = bookRepository.findByAuthorAndTitleContainingIgnoreCase(
                        author, title, pageable
                );
            } else if (yearPublished != null) {
                books = bookRepository.findByAuthorAndYearPublished(
                        author, yearPublished, pageable
                );
            } else {
                books = bookRepository.findByAuthor(author, pageable);
            }
        } else {
            books = bookRepository.findAll(pageable);
        }

        List<BookResponseDto> list = books.stream()
                .map(BookMapper::toDto)
                .toList();

        return PaginatedResponseDto.<BookResponseDto>builder()
                .list(list)
                .totalPages(books.getTotalPages())
                .build();
    }

    public byte[] generateCsvReport(Long authorId, String title, Integer yearPublished) throws IOException {
        List<BookResponseDto> books =
                getList(authorId, title, yearPublished, 1, Integer.MAX_VALUE).getList();

        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);

        csvWriter.writeNext(new String[]{"ID", "Title", "Author", "Year Published", "Genres"});

        for (BookResponseDto book : books) {
            csvWriter.writeNext(new String[]{
                    String.valueOf(book.getId()),
                    book.getTitle(),
                    book.getAuthor().getName(),
                    String.valueOf(book.getYearPublished()),
                    book.getGenres() == null ? "" : String.join("|", book.getGenres())
            });
        }

        csvWriter.close();

        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    public UploadResponseDto uploadFromJson(MultipartFile file) throws IOException {
        int success = 0;
        int failed = 0;

        List<BookRequestDto> items =
                Arrays.asList(objectMapper.readValue(file.getInputStream(), BookRequestDto[].class));

        for (BookRequestDto dto : items) {
            try {
                create(dto);
                success++;
            } catch (Exception e) {
                failed++;
            }
        }

        return new UploadResponseDto(success, failed);
    }
}

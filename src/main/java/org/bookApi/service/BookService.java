package org.bookApi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;

    private List<String> safeGenres(List<String> genres) {
        return genres == null ? new ArrayList<>() : new ArrayList<>(genres);
    }


    public BookResponseDto create(BookRequestDto dto) {
        Author author = getAuthorOrThrow(dto.authorId());
        Book book = Book.builder()
                .title(dto.title())
                .author(author)
                .yearPublished(dto.yearPublished())
                .genres(safeGenres(dto.genres()))
                .build();
        return BookMapper.toDto(bookRepository.save(book));
    }


    public BookResponseDto update(Long id, BookRequestDto dto) {
        Book book = getBookOrThrow(id);
        Author author = getAuthorOrThrow(dto.authorId());

        book.setTitle(dto.title());
        book.setAuthor(author);
        book.setYearPublished(dto.yearPublished());
        book.setGenres(safeGenres(dto.genres()));

        return BookMapper.toDto(bookRepository.save(book));
    }


    public void delete(Long id) {
        Book book = getBookOrThrow(id);
        bookRepository.delete(book);
    }


    @Transactional(readOnly = true)
    public BookResponseDto getById(Long id) {
        return BookMapper.toDto(getBookOrThrow(id));
    }


    @Transactional(readOnly = true)
    public List<BookResponseDto> getAll() {
        return bookRepository.findAll().stream()
                .map(BookMapper::toDto)
                .toList();
    }


    @Transactional(readOnly = true)
    public PaginatedResponseDto<BookResponseDto> getList(
            Long authorId, String title, Integer year, int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("title"));

        Specification<Book> spec = Specification.allOf(
                withAuthor(authorId),
                withTitle(title),
                withYear(year)
        );

        Page<Book> books = bookRepository.findAll(spec, pageable);
        List<BookResponseDto> list = books.stream().map(BookMapper::toDto).toList();
        return new PaginatedResponseDto<>(list, books.getTotalPages());
    }


    public byte[] generateCsvReport(Long authorId, String title, Integer year) throws IOException {
        List<BookResponseDto> books = getList(authorId, title, year, 1, Integer.MAX_VALUE).list();
        StringWriter writer = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(new String[]{"ID", "Title", "Author", "Year Published", "Genres"});
            for (BookResponseDto book : books) {
                csvWriter.writeNext(new String[]{
                        String.valueOf(book.id()),
                        book.title(),
                        book.author().name(),
                        String.valueOf(book.yearPublished()),
                        book.genres() == null ? "" : String.join("|", book.genres())
                });
            }
        }
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }


    public UploadResponseDto uploadFromJson(MultipartFile file) throws IOException {
        int success = 0, failed = 0;
        List<BookRequestDto> items;

        try {
            items = Arrays.asList(objectMapper.readValue(file.getInputStream(), BookRequestDto[].class));
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON file");
        }

        for (BookRequestDto dto : items) {
            try {
                create(dto);
                success++;
            } catch (Exception ex) {
                failed++;
                log.warn("Failed to import book '{}': {}", dto.title(), ex.getMessage());
            }
        }

        return new UploadResponseDto(success, failed);
    }


    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private Author getAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
    }

    private Specification<Book> withAuthor(Long authorId) {
        return (root, cq, cb) -> authorId == null ? null : cb.equal(root.get("author").get("id"), authorId);
    }

    private Specification<Book> withTitle(String title) {
        return (root, cq, cb) -> (title == null || title.isBlank()) ? null
                : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<Book> withYear(Integer year) {
        return (root, cq, cb) -> year == null ? null : cb.equal(root.get("yearPublished"), year);
    }
}

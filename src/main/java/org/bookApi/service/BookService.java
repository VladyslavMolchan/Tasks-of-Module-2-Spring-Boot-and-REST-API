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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;



@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;

    // ---------------- CREATE ----------------

    public BookResponseDto create(BookRequestDto dto) {
        log.info("Creating book with title: '{}' and authorId: {}", dto.title(), dto.authorId());
        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> {
                    log.error("Book creation failed. Author with id {} not found", dto.authorId());
                    return new ResourceNotFoundException("Author not found");
                });

        Book book = Book.builder()
                .title(dto.title())
                .author(author)
                .yearPublished(dto.yearPublished())
                .genres(dto.genres() == null ? List.of() : dto.genres())
                .build();

        BookResponseDto createdBook = BookMapper.toDto(bookRepository.save(book));
        log.info("Book created successfully with id: {}", createdBook.id());
        return createdBook;
    }

    // ---------------- UPDATE ----------------

    public BookResponseDto update(Long id, BookRequestDto dto) {
        log.info("Updating book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book update failed. Book with id {} not found", id);
                    return new ResourceNotFoundException("Book not found");
                });

        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> {
                    log.error("Book update failed. Author with id {} not found", dto.authorId());
                    return new ResourceNotFoundException("Author not found");
                });

        book.setTitle(dto.title());
        book.setAuthor(author);
        book.setYearPublished(dto.yearPublished());
        book.setGenres(dto.genres() == null ? List.of() : dto.genres());

        BookResponseDto updatedBook = BookMapper.toDto(bookRepository.save(book));
        log.info("Book updated successfully with id: {}", updatedBook.id());
        return updatedBook;
    }

    // ---------------- DELETE ----------------

    public void delete(Long id) {
        log.info("Deleting book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book deletion failed. Book with id {} not found", id);
                    return new ResourceNotFoundException("Book not found");
                });
        bookRepository.delete(book);
        log.info("Book deleted successfully with id: {}", id);
    }

    // ---------------- GET BY ID ----------------

    public BookResponseDto getById(Long id) {
        log.info("Fetching book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book fetch failed. Book with id {} not found", id);
                    return new ResourceNotFoundException("Book not found");
                });
        BookResponseDto dto = BookMapper.toDto(book);
        log.info("Fetched book: {}", dto);
        return dto;
    }

    // ---------------- LIST WITH FILTERS ----------------

    public PaginatedResponseDto<BookResponseDto> getList(
            Long authorId,
            String title,
            Integer yearPublished,
            int page,
            int size
    ) {
        log.info("Fetching books with filters - authorId: {}, title: {}, yearPublished: {}, page: {}, size: {}",
                authorId, title, yearPublished, page, size);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("title"));

        Specification<Book> spec = Specification.allOf(
                withAuthor(authorId),
                withTitle(title),
                withYear(yearPublished)
        );

        Page<Book> books = bookRepository.findAll(spec, pageable);
        List<BookResponseDto> list = books.stream()
                .map(BookMapper::toDto)
                .toList();

        log.info("Fetched {} books, total pages: {}", list.size(), books.getTotalPages());
        return new PaginatedResponseDto<>(list, books.getTotalPages());
    }

    // ---------------- SPECIFICATIONS ----------------

    private Specification<Book> withAuthor(Long authorId) {
        return (root, cq, cb) ->
                authorId == null ? null :
                        cb.equal(root.get("author").get("id"), authorId);
    }

    private Specification<Book> withTitle(String title) {
        return (root, cq, cb) ->
                (title == null || title.isBlank()) ? null :
                        cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<Book> withYear(Integer year) {
        return (root, cq, cb) ->
                year == null ? null :
                        cb.equal(root.get("yearPublished"), year);
    }

    // ---------------- CSV REPORT ----------------

    public byte[] generateCsvReport(Long authorId, String title, Integer yearPublished) throws IOException {
        log.info("Generating CSV report with filters - authorId: {}, title: {}, yearPublished: {}",
                authorId, title, yearPublished);
        List<BookResponseDto> books = getList(authorId, title, yearPublished, 1, Integer.MAX_VALUE).list();

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

        log.info("CSV report generated, size: {} bytes", writer.toString().getBytes(StandardCharsets.UTF_8).length);
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ---------------- JSON UPLOAD ----------------

    public UploadResponseDto uploadFromJson(MultipartFile file) throws IOException {
        log.info("Uploading books from file: {}", file.getOriginalFilename());
        int success = 0;
        int failed = 0;

        List<BookRequestDto> items;
        try {
            items = Arrays.asList(objectMapper.readValue(file.getInputStream(), BookRequestDto[].class));
        } catch (Exception e) {
            log.error("Failed to parse JSON file", e);
            throw new RuntimeException("Invalid JSON file");
        }

        for (BookRequestDto dto : items) {
            try {
                create(dto);
                success++;
            } catch (Exception e) {
                failed++;
                log.warn("Failed to import book '{}': {}", dto.title(), e.getMessage());
            }
        }

        log.info("Upload completed. Successful: {}, Failed: {}", success, failed);
        return new UploadResponseDto(success, failed);
    }
}

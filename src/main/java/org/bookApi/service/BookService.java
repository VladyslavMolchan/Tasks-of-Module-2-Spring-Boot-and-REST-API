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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;

    private List<String> safeGenres(List<String> genres) {
        return genres == null ? new ArrayList<>() : new ArrayList<>(genres);
    }

    public BookResponseDto create(BookRequestDto dto) {
        log.info("Creating book with title '{}' and authorId {}", dto.title(), dto.authorId());

        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Book book = Book.builder()
                .title(dto.title())
                .author(author)
                .yearPublished(dto.yearPublished())
                .genres(safeGenres(dto.genres()))
                .build();

        return BookMapper.toDto(bookRepository.save(book));
    }

    public BookResponseDto createOrDefaultAuthor(BookRequestDto dto) {

        // Якщо authorId == null → використовуємо дефолтного
        if (dto.authorId() == null) {
            Author defaultAuthor = authorRepository.findByName("Default Author")
                    .orElseGet(() -> authorRepository.save(Author.builder().name("Default Author").build()));

            dto = new BookRequestDto(
                    dto.title(),
                    defaultAuthor.getId(),
                    dto.yearPublished(),
                    dto.genres()
            );

            return create(dto);
        }

        if (!authorRepository.existsById(dto.authorId())) {
            throw new ResourceNotFoundException("Author not found");
        }

        return create(dto);
    }


    public BookResponseDto update(Long id, BookRequestDto dto) {

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        book.setTitle(dto.title());
        book.setAuthor(author);
        book.setYearPublished(dto.yearPublished());
        book.setGenres(safeGenres(dto.genres()));

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

    public List<BookResponseDto> getAll() {
        log.info("Fetching all books");
        List<BookResponseDto> books = bookRepository.findAll().stream()
                .map(BookMapper::toDto)
                .toList();
        log.info("Fetched {} books", books.size());
        return books;
    }

    public BookResponseDto getByIdOrDefault(Long id) {

        return bookRepository.findById(id)
                .map(BookMapper::toDto)
                .orElseGet(() -> {

                    Author defaultAuthor = authorRepository.findByName("Default Author")
                            .orElseGet(() -> authorRepository.save(
                                    Author.builder().name("Default Author").build()
                            ));

                    Book defaultBook = Book.builder()
                            .title("Default Book")
                            .author(defaultAuthor)
                            .yearPublished(LocalDate.now().getYear())
                            .genres(new ArrayList<>())
                            .build();

                    return BookMapper.toDto(defaultBook);
                });
    }

    public PaginatedResponseDto<BookResponseDto> getList(
            Long authorId, String title, Integer year, int page, int size
    ) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("title"));

        Specification<Book> spec = Specification.allOf(
                withAuthor(authorId),
                withTitle(title),
                withYear(year)
        );

        Page<Book> books = bookRepository.findAll(spec, pageable);

        List<BookResponseDto> list = books.stream()
                .map(BookMapper::toDto)
                .toList();

        return new PaginatedResponseDto<>(list, books.getTotalPages());
    }

    private Specification<Book> withAuthor(Long authorId) {
        return (root, cq, cb) -> authorId == null
                ? null
                : cb.equal(root.get("author").get("id"), authorId);
    }

    private Specification<Book> withTitle(String title) {
        return (root, cq, cb) ->
                (title == null || title.isBlank())
                        ? null
                        : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<Book> withYear(Integer year) {
        return (root, cq, cb) -> year == null
                ? null
                : cb.equal(root.get("yearPublished"), year);
    }

    public byte[] generateCsvReport(Long authorId, String title, Integer year) throws IOException {

        List<BookResponseDto> books = getList(authorId, title, year, 1, Integer.MAX_VALUE).list();

        StringWriter writer = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(writer)) {

            csvWriter.writeNext(new String[]{
                    "ID", "Title", "Author", "Year Published", "Genres"
            });

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
            items = Arrays.asList(
                    objectMapper.readValue(file.getInputStream(), BookRequestDto[].class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON file");
        }

        for (BookRequestDto dto : items) {
            try {
                dto = new BookRequestDto(
                        dto.title(),
                        dto.authorId(),
                        dto.yearPublished(),
                        safeGenres(dto.genres())
                );

                createOrDefaultAuthor(dto);
                success++;
            } catch (Exception ex) {
                failed++;
                log.warn("Failed to import book '{}': {}", dto.title(), ex.getMessage());
            }
        }

        return new UploadResponseDto(success, failed);
    }
}

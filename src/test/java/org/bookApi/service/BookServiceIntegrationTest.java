package org.bookApi.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookApi.dto.BookRequestDto;
import org.bookApi.dto.BookResponseDto;
import org.bookApi.dto.UploadResponseDto;
import org.bookApi.entity.Author;
import org.bookApi.entity.Book;
import org.bookApi.exception.ResourceNotFoundException;
import org.bookApi.repository.AuthorRepository;
import org.bookApi.repository.BookRepository;
import org.bookApi.dto.PaginatedResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private Author savedAuthor;

    @BeforeEach
    void init() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();

        savedAuthor = authorRepository.save(
                Author.builder().name("Main Author").build()
        );
    }

    @Test
    void create_success() {
        BookRequestDto dto = new BookRequestDto("Book 1", savedAuthor.getId(),
                2020,
                new ArrayList<>(List.of("Drama")));

        BookResponseDto result = bookService.create(dto);

        assertNotNull(result.id());
        assertEquals("Book 1", result.title());
        assertEquals(savedAuthor.getId(), result.author().id());
        assertEquals(1, bookRepository.count());
    }

    @Test
    void create_authorNotFound_throws() {
        BookRequestDto dto = new BookRequestDto("Fail", 999L, 2020, new ArrayList<>());

        assertThrows(ResourceNotFoundException.class, () -> bookService.create(dto));
    }

    @Test
    void createOrDefaultAuthor_usesDefaultAuthorWhenMissing() {

        BookRequestDto dto = new BookRequestDto("Book X", null, 2021, new ArrayList<>());

        BookResponseDto result = bookService.createOrDefaultAuthor(dto);

        assertEquals("Book X", result.title());
        assertEquals("Default Author", result.author().name());
        assertEquals(2, authorRepository.count());
    }

    @Test
    void createOrDefaultAuthor_usesExistingAuthor() {

        BookRequestDto dto =
                new BookRequestDto("Book Y", savedAuthor.getId(), 2021,
                        new ArrayList<>(List.of("Action")));

        BookResponseDto result = bookService.createOrDefaultAuthor(dto);

        assertEquals(savedAuthor.getId(), result.author().id());
    }

    @Test
    void getById_success() {
        Book saved = bookRepository.save(
                Book.builder()
                        .title("Existing")
                        .author(savedAuthor)
                        .yearPublished(2020)
                        .genres(new ArrayList<>())
                        .build()
        );

        BookResponseDto dto = bookService.getById(saved.getId());

        assertEquals("Existing", dto.title());
    }

    @Test
    void getById_notFound() {
        assertThrows(ResourceNotFoundException.class, () -> bookService.getById(777L));
    }

    @Test
    void getAll_success() {
        bookRepository.save(Book.builder().title("B1").author(savedAuthor)
                .yearPublished(2020).genres(new ArrayList<>()).build());

        bookRepository.save(Book.builder().title("B2").author(savedAuthor)
                .yearPublished(2021).genres(new ArrayList<>()).build());

        List<BookResponseDto> result = bookService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getByIdOrDefault_returnsBook() {
        Book book = bookRepository.save(
                Book.builder()
                        .title("ZBook")
                        .author(savedAuthor)
                        .yearPublished(2022)
                        .genres(new ArrayList<>())
                        .build()
        );

        BookResponseDto result = bookService.getByIdOrDefault(book.getId());

        assertEquals("ZBook", result.title());
    }

    @Test
    void getByIdOrDefault_returnsDefault() {
        BookResponseDto dto = bookService.getByIdOrDefault(9999L);

        assertEquals("Default Book", dto.title());
        assertEquals("Default Author", dto.author().name());
        assertEquals(LocalDate.now().getYear(), dto.yearPublished());
    }

    @Test
    void update_success() {
        Book book = bookRepository.save(
                Book.builder()
                        .title("Old Title")
                        .author(savedAuthor)
                        .yearPublished(1999)
                        .genres(new ArrayList<>(List.of("SciFi")))
                        .build()
        );

        Author newAuthor = authorRepository.save(
                Author.builder().name("New Author").build()
        );

        BookRequestDto dto = new BookRequestDto("New Title",
                newAuthor.getId(),
                2022,
                new ArrayList<>(List.of("Fantasy")));

        BookResponseDto updated = bookService.update(book.getId(), dto);

        assertEquals("New Title", updated.title());
        assertEquals("New Author", updated.author().name());
        assertEquals(2022, updated.yearPublished());
    }

    @Test
    void update_bookNotFound() {
        BookRequestDto dto = new BookRequestDto("ABC", savedAuthor.getId(), 2000, new ArrayList<>());
        assertThrows(ResourceNotFoundException.class, () -> bookService.update(123L, dto));
    }

    @Test
    void update_authorNotFound() {
        Book saved = bookRepository.save(Book.builder()
                .title("TT")
                .author(savedAuthor)
                .yearPublished(2000)
                .genres(new ArrayList<>())
                .build());

        BookRequestDto dto = new BookRequestDto("New", 999L, 2001, new ArrayList<>());

        assertThrows(ResourceNotFoundException.class, () -> bookService.update(saved.getId(), dto));
    }

    @Test
    void delete_success() {
        Book book = bookRepository.save(
                Book.builder().title("Del").author(savedAuthor)
                        .yearPublished(2000).genres(new ArrayList<>()).build()
        );

        bookService.delete(book.getId());

        assertEquals(0, bookRepository.count());
    }

    @Test
    void delete_notFound() {
        assertThrows(ResourceNotFoundException.class, () -> bookService.delete(555L));
    }

    @Test
    void getList_searchByTitle() {
        bookRepository.save(Book.builder().title("Alpha")
                .author(savedAuthor).yearPublished(2001).genres(new ArrayList<>()).build());

        bookRepository.save(Book.builder().title("Beta")
                .author(savedAuthor).yearPublished(2002).genres(new ArrayList<>()).build());

        PaginatedResponseDto<BookResponseDto> page =
                bookService.getList(null, "alph", null, 1, 20);

        assertEquals(1, page.list().size());
        assertEquals("Alpha", page.list().get(0).title());
    }

    @Test
    void getList_searchByAuthor() {
        Author a2 = authorRepository.save(
                Author.builder().name("Other").build()
        );

        bookRepository.save(Book.builder().title("A1").author(savedAuthor)
                .yearPublished(2000).genres(new ArrayList<>()).build());

        bookRepository.save(Book.builder().title("A2").author(a2)
                .yearPublished(2001).genres(new ArrayList<>()).build());

        PaginatedResponseDto<BookResponseDto> page =
                bookService.getList(a2.getId(), null, null, 1, 20);

        assertEquals(1, page.list().size());
        assertEquals("A2", page.list().get(0).title());
    }

    @Test
    void generateCsvReport_success() throws IOException {
        bookRepository.save(Book.builder().title("CSVBook").author(savedAuthor)
                .yearPublished(2000)
                .genres(new ArrayList<>(List.of("A","B"))).build());

        byte[] data = bookService.generateCsvReport(null, null, null);

        String csv = new String(data, StandardCharsets.UTF_8);

        assertTrue(csv.contains("CSVBook"));
        assertTrue(csv.contains("A|B"));
    }

    @Test
    void uploadFromJson_successAndFailedMix() throws Exception {
        BookRequestDto[] items = {
                new BookRequestDto("Good", savedAuthor.getId(), 2000,
                        new ArrayList<>(List.of("X"))),
                new BookRequestDto("BadNoAuthor", 999L, 2020, new ArrayList<>())
        };

        byte[] json = objectMapper.writeValueAsBytes(items);

        MockMultipartFile file = new MockMultipartFile(
                "file", "data.json", MediaType.APPLICATION_JSON_VALUE, json
        );

        UploadResponseDto resp = bookService.uploadFromJson(file);

        assertEquals(1, resp.successCount());
        assertEquals(1, resp.failedCount());
    }

    @Test
    void uploadFromJson_invalidJson_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bad.json", MediaType.APPLICATION_JSON_VALUE,
                "{ invalid json".getBytes()
        );

        assertThrows(RuntimeException.class, () -> bookService.uploadFromJson(file));
    }
}

package org.bookApi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookApi.dto.*;
import org.bookApi.entity.Author;
import org.bookApi.entity.Book;
import org.bookApi.repository.AuthorRepository;
import org.bookApi.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Author author;

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        author = authorRepository.save(Author.builder().name("Test Author").build());
        bookRepository.save(Book.builder().title("Book One").author(author).yearPublished(2020).build());
    }

    @Test
    void testCreateBook() {
        BookRequestDto request = new BookRequestDto("Book Two", author.getId(), 2021, List.of("Fiction"));

        ResponseEntity<BookResponseDto> response = restTemplate.postForEntity("/api/books", request, BookResponseDto.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);


        assertThat(response.getBody().title()).isEqualTo("Book Two");


        assertThat(bookRepository.findAll()).hasSize(2);
    }

    @Test
    void testGetBookById() {
        Book existing = bookRepository.findAll().get(0);

        ResponseEntity<BookResponseDto> response = restTemplate.getForEntity("/api/books/" + existing.getId(), BookResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo(existing.getTitle());
    }

    @Test
    void testUpdateBook() {
        Book existing = bookRepository.findAll().get(0);
        BookRequestDto updateRequest = new BookRequestDto("Updated Book", author.getId(), 2022, List.of("Mystery"));

        HttpEntity<BookRequestDto> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<BookResponseDto> response = restTemplate.exchange("/api/books/" + existing.getId(), HttpMethod.PUT, entity, BookResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().title()).isEqualTo("Updated Book");
    }

    @Test
    void testDeleteBook() {
        Book existing = bookRepository.findAll().get(0);

        ResponseEntity<Void> response = restTemplate.exchange("/api/books/" + existing.getId(),
                HttpMethod.DELETE, null, Void.class);


        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);


        assertThat(bookRepository.existsById(existing.getId())).isFalse();
    }

    @Test
    void testSearchBooks() {
        BookRequestDto request = new BookRequestDto("Search Book", author.getId(), 2021, List.of("Fiction"));
        bookRepository.save(Book.builder().title(request.title()).author(author).yearPublished(request.yearPublished()).build());

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", "Search");

        HttpEntity<Void> entity = new HttpEntity<>(null, new HttpHeaders());
        ResponseEntity<PaginatedResponseDto> response = restTemplate.postForEntity("/api/books/search?title=Search", entity, PaginatedResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().list()).hasSizeGreaterThan(0);
    }

    @Test
    void testGenerateCsvReport() {
        ResponseEntity<byte[]> response = restTemplate.postForEntity("/api/books/_report", null, byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String content = new String(response.getBody(), StandardCharsets.UTF_8);
        assertThat(content).contains("ID", "Title", "Author");
    }

    @Test
    void testUploadBooksFromJson() throws Exception {
        BookRequestDto[] books = { new BookRequestDto("Uploaded Book", author.getId(), 2022, List.of("Sci-Fi")) };
        byte[] jsonBytes = objectMapper.writeValueAsBytes(books);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource resource = new ByteArrayResource(jsonBytes) {
            @Override
            public String getFilename() {
                return "books.json";
            }
        };
        body.add("file", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<UploadResponseDto> response = restTemplate.postForEntity("/api/books/_upload", requestEntity, UploadResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().successCount()).isEqualTo(1);
    }

    @Test
    void testGetAllBooksPaginated() {
        ResponseEntity<PaginatedResponseDto<BookResponseDto>> response = restTemplate.exchange(
                "/api/books?page=1&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponseDto<BookResponseDto>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<BookResponseDto> books = response.getBody().list();
        assertThat(books).hasSizeGreaterThan(0);
    }

}

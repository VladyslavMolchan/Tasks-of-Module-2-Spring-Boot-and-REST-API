package org.bookApi.controller;

import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import org.bookApi.entity.Author;
import org.bookApi.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthorControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setup() {
        authorRepository.deleteAll();
        authorRepository.save(Author.builder().name("Author One").build());
        authorRepository.save(Author.builder().name("Author Two").build());
    }


    @Test
    void testGetAllAuthorsPaginated() {
        ResponseEntity<PaginatedResponseDto<AuthorResponseDto>> response = restTemplate.exchange(
                "/api/authors?page=1&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<PaginatedResponseDto<AuthorResponseDto>>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<AuthorResponseDto> authors = response.getBody().list();
        assertThat(authors).hasSize(2);
    }

    @Test
    void testGetAuthorById() {
        Author existing = authorRepository.findAll().get(0);

        ResponseEntity<AuthorResponseDto> response = restTemplate.getForEntity(
                "/api/authors/" + existing.getId(), AuthorResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo(existing.getName());
    }

    @Test
    void testGetAuthorByIdNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/authors/9999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Author not found");
    }


    @Test
    void testCreateAuthor() {
        AuthorRequestDto request = new AuthorRequestDto("Author Three");

        ResponseEntity<AuthorResponseDto> response =
                restTemplate.postForEntity("/api/authors", request, AuthorResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().name()).isEqualTo("Author Three");

        List<Author> allAuthors = authorRepository.findAll();
        assertThat(allAuthors).hasSize(3);
    }

    @Test
    void testCreateAuthorDuplicateName() {
        AuthorRequestDto request = new AuthorRequestDto("Author One");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/authors", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("already exists");
    }


    @Test
    void testUpdateAuthor() {
        Author existing = authorRepository.findAll().get(0);
        AuthorRequestDto updateRequest = new AuthorRequestDto("Updated Name");

        HttpEntity<AuthorRequestDto> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<AuthorResponseDto> response = restTemplate.exchange(
                "/api/authors/" + existing.getId(),
                HttpMethod.PUT,
                entity,
                AuthorResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("Updated Name");
    }

    @Test
    void testUpdateAuthorNotFound() {
        AuthorRequestDto updateRequest = new AuthorRequestDto("Does Not Exist");

        HttpEntity<AuthorRequestDto> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/authors/9999",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Author not found");
    }


    @Test
    void testDeleteAuthor() {
        Author existing = authorRepository.findAll().get(0);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/authors/" + existing.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(authorRepository.existsById(existing.getId())).isFalse();
    }

    @Test
    void testDeleteAuthorNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/authors/9999",
                HttpMethod.DELETE,
                null,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Author not found");
    }
}
package org.bookApi.controller;

import lombok.RequiredArgsConstructor;
import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.service.AuthorService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Slf4j
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public List<AuthorResponseDto> getAll() {
        log.info("Fetching all authors");
        List<AuthorResponseDto> authors = authorService.getAll();
        log.info("Fetched {} authors", authors.size());
        return authors;
    }

    @PostMapping
    public AuthorResponseDto create(@Valid @RequestBody AuthorRequestDto dto) {
        log.info("Creating a new author: {}", dto);
        AuthorResponseDto createdAuthor = authorService.create(dto);
        log.info("Author created with id: {}", createdAuthor.id());
        return createdAuthor;
    }

    @PutMapping("/{id}")
    public AuthorResponseDto update(@PathVariable Long id, @Valid @RequestBody AuthorRequestDto dto) {
        log.info("Updating author with id {}: {}", id, dto);
        AuthorResponseDto updatedAuthor = authorService.update(id, dto);
        log.info("Author updated with id: {}", updatedAuthor.id());
        return updatedAuthor;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Deleting author with id {}", id);
        authorService.delete(id);
        log.info("Author deleted with id {}", id);
    }
}

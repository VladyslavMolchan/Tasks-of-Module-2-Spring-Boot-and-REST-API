package org.bookApi.service;


import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.entity.Author;
import org.bookApi.exception.ResourceNotFoundException;
import org.bookApi.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    void setup() {
        authorRepository.deleteAll();
    }

    @Test
    void createAuthor_success() {
        AuthorRequestDto dto = new AuthorRequestDto("New Author");

        AuthorResponseDto result = authorService.create(dto);

        assertNotNull(result.id());
        assertEquals("New Author", result.name());
        assertEquals(1, authorRepository.count());
    }

    @Test
    void createAuthor_duplicateName_throws() {
        authorRepository.save(Author.builder().name("John").build());
        AuthorRequestDto dto = new AuthorRequestDto("John");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authorService.create(dto));

        assertEquals("Author with name 'John' already exists", exception.getMessage());
    }

    @Test
    void getAll_returnsAuthors() {
        authorRepository.save(Author.builder().name("A").build());
        authorRepository.save(Author.builder().name("B").build());

        List<AuthorResponseDto> list = authorService.getAll();

        assertEquals(2, list.size());
    }


    @Test
    void createOrReturnExisting_returnsExisting() {
        authorRepository.save(Author.builder().name("Bob").build());

        AuthorRequestDto dto = new AuthorRequestDto("Bob");

        AuthorResponseDto result = authorService.createOrReturnExisting(dto);

        assertEquals("Bob", result.name());
        assertEquals(1, authorRepository.count());
    }

    @Test
    void createOrReturnExisting_createsNew() {
        AuthorRequestDto dto = new AuthorRequestDto("Chris");

        AuthorResponseDto result = authorService.createOrReturnExisting(dto);

        assertEquals("Chris", result.name());
        assertEquals(1, authorRepository.count());
    }

    @Test
    void updateAuthor_success() {
        Author author = authorRepository.save(Author.builder().name("Old").build());
        AuthorRequestDto dto = new AuthorRequestDto("New");

        AuthorResponseDto updated = authorService.update(author.getId(), dto);

        assertEquals("New", updated.name());
    }

    @Test
    void updateAuthor_notFound_throws() {
        AuthorRequestDto dto = new AuthorRequestDto("Something");

        assertThrows(ResourceNotFoundException.class,
                () -> authorService.update(555L, dto));
    }

    @Test
    void updateAuthor_duplicateName_throws() {
        Author a1 = authorRepository.save(Author.builder().name("A").build());
        authorRepository.save(Author.builder().name("B").build());

        AuthorRequestDto dto = new AuthorRequestDto("B");

        assertThrows(IllegalArgumentException.class,
                () -> authorService.update(a1.getId(), dto));
    }

    @Test
    void deleteAuthor_success() {
        Author author = authorRepository.save(Author.builder().name("ToDelete").build());

        authorService.delete(author.getId());

        assertEquals(0, authorRepository.count());
    }

    @Test
    void deleteAuthor_notFound_throws() {
        assertThrows(ResourceNotFoundException.class,
                () -> authorService.delete(999L));
    }
}

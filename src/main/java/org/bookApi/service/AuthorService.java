package org.bookApi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.entity.Author;
import org.bookApi.exception.ResourceNotFoundException;
import org.bookApi.repository.AuthorRepository;
import org.bookApi.mapper.AuthorMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<AuthorResponseDto> getAll() {
        log.info("Fetching all authors");
        List<AuthorResponseDto> authors = authorRepository.findAll().stream()
                .map(AuthorMapper::toDto)
                .toList();
        log.info("Fetched {} authors", authors.size());
        return authors;
    }

    public AuthorResponseDto create(AuthorRequestDto dto) {
        log.info("Creating author with name: {}", dto.name());
        if (authorRepository.existsByName(dto.name())) {
            log.error("Author creation failed. Name '{}' already exists", dto.name());
            throw new IllegalArgumentException("Author with name '" + dto.name() + "' already exists");
        }

        Author author = Author.builder()
                .name(dto.name())
                .build();

        AuthorResponseDto createdAuthor = AuthorMapper.toDto(authorRepository.save(author));
        log.info("Author created successfully with id: {}", createdAuthor.id());
        return createdAuthor;
    }

    public AuthorResponseDto update(Long id, AuthorRequestDto dto) {
        log.info("Updating author with id: {} and new name: {}", id, dto.name());
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Author update failed. Author with id {} not found", id);
                    return new ResourceNotFoundException("Author not found");
                });

        if (!author.getName().equals(dto.name()) && authorRepository.existsByName(dto.name())) {
            log.error("Author update failed. Name '{}' already exists", dto.name());
            throw new IllegalArgumentException("Author with name '" + dto.name() + "' already exists");
        }

        author.setName(dto.name());
        AuthorResponseDto updatedAuthor = AuthorMapper.toDto(authorRepository.save(author));
        log.info("Author updated successfully with id: {}", updatedAuthor.id());
        return updatedAuthor;
    }

    public void delete(Long id) {
        log.info("Deleting author with id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Author deletion failed. Author with id {} not found", id);
                    return new ResourceNotFoundException("Author not found");
                });
        authorRepository.delete(author);
        log.info("Author deleted successfully with id: {}", id);
    }
}

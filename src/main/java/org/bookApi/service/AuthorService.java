package org.bookApi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.bookApi.entity.Author;
import org.bookApi.exception.ResourceNotFoundException;
import org.bookApi.repository.AuthorRepository;
import org.bookApi.mapper.AuthorMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<AuthorResponseDto> getAll() {
        List<Author> authors = authorRepository.findAll();
        log.info("Fetched {} authors", authors.size());
        return authors.stream()
                .map(AuthorMapper::toDto)
                .toList();
    }

    public AuthorResponseDto getById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
        return AuthorMapper.toDto(author);
    }

    @Transactional
    public AuthorResponseDto create(AuthorRequestDto dto) {
        if (authorRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Author with name '" + dto.name() + "' already exists");
        }
        Author saved = authorRepository.save(
                Author.builder().name(dto.name()).build()
        );
        return AuthorMapper.toDto(saved);
    }

    @Transactional
    public AuthorResponseDto createOrReturnExisting(AuthorRequestDto dto) {
        return authorRepository.findByName(dto.name())
                .map(AuthorMapper::toDto)
                .orElseGet(() -> create(dto));
    }

    @Transactional
    public AuthorResponseDto update(Long id, AuthorRequestDto dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));

        if (!author.getName().equals(dto.name())
                && authorRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Author with name '" + dto.name() + "' already exists");
        }

        author.setName(dto.name());
        return AuthorMapper.toDto(author);
    }

    @Transactional
    public void delete(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
        authorRepository.delete(author);
    }

    @Transactional(readOnly = true)
    public PaginatedResponseDto<AuthorResponseDto> getList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name"));
        Page<Author> authors = authorRepository.findAll(pageable);
        List<AuthorResponseDto> list = authors.stream()
                .map(AuthorMapper::toDto)
                .toList();
        return new PaginatedResponseDto<>(list, authors.getTotalPages());
    }

}

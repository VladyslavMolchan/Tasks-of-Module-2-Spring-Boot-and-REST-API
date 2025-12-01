package org.bookApi.service;


import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.entity.Author;
import org.bookApi.exception.ResourceNotFoundException;
import org.bookApi.mapper.AuthorMapper;
import org.bookApi.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;


    public AuthorResponseDto create(AuthorRequestDto dto) {
        if (authorRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Author with this name already exists");
        }

        Author author = AuthorMapper.fromRequestDto(dto);
        return AuthorMapper.toDto(authorRepository.save(author));
    }


    public AuthorResponseDto update(Long id, AuthorRequestDto dto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        if (!author.getName().equals(dto.getName()) &&
                authorRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Author with this name already exists");
        }

        AuthorMapper.updateEntity(author, dto);
        return AuthorMapper.toDto(authorRepository.save(author));
    }


    public void delete(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        authorRepository.delete(author);
    }


    public List<AuthorResponseDto> getAll() {
        return authorRepository.findAll().stream()
                .map(AuthorMapper::toDto)
                .toList();
    }


    public Optional<AuthorResponseDto> findById(Long id) {
        return authorRepository.findById(id)
                .map(AuthorMapper::toDto);
    }


    public AuthorResponseDto getById(Long id) {
        return authorRepository.findById(id)
                .map(AuthorMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
    }
}

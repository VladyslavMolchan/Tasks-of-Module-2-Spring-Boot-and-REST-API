package org.bookApi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.bookApi.service.AuthorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Slf4j
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Get authors with pagination", description = "Returns a paginated list of authors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved authors",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedResponseDto.class)))
    })
    @GetMapping
    public PaginatedResponseDto<AuthorResponseDto> getAll(
            @Parameter(description = "Page number, default 1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size, default 4") @RequestParam(defaultValue = "4") int size) {
        log.info("Fetching authors, page={}, size={}", page, size);
        return authorService.getList(page, size);
    }

    @Operation(summary = "Get an author by ID", description = "Returns a single author by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> getById(@PathVariable Long id) {
        log.info("Fetching author with id {}", id);
        AuthorResponseDto dto = authorService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Create a new author", description = "Creates a new author")
    @PostMapping
    public ResponseEntity<AuthorResponseDto> create(@Valid @RequestBody AuthorRequestDto dto) {
        log.info("Creating a new author: {}", dto);
        AuthorResponseDto created = authorService.create(dto);
        return ResponseEntity
                .created(URI.create("/api/authors/" + created.id()))
                .body(created);
    }

    @Operation(summary = "Update an existing author", description = "Updates the author with the specified ID")
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDto> update(@PathVariable Long id,
                                                    @Valid @RequestBody AuthorRequestDto dto) {
        log.info("Updating author with id {}: {}", id, dto);
        AuthorResponseDto updated = authorService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete an author", description = "Deletes the author with the specified ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting author with id {}", id);
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

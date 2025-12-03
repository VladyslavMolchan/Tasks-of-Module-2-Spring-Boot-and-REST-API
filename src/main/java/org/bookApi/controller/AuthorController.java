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
import org.bookApi.service.AuthorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Slf4j
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Get all authors", description = "Returns a list of all authors in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorResponseDto.class)))
    })
    @GetMapping
    public List<AuthorResponseDto> getAll() {
        log.info("Fetching all authors");
        return authorService.getAll();
    }

    @Operation(summary = "Get an author by ID", description = "Returns a single author by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{id}")
    public AuthorResponseDto getById(
            @Parameter(description = "ID of the author to fetch", required = true)
            @PathVariable Long id) {
        log.info("Fetching author with id {}", id);
        return authorService.getByIdOrDefault(id); // повертає дефолтного автора, якщо не існує
    }

    @Operation(summary = "Create a new author", description = "Creates a new author with the given details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public AuthorResponseDto create(
            @Parameter(description = "Author details for creation", required = true)
            @Valid @RequestBody AuthorRequestDto dto) {
        log.info("Creating a new author: {}", dto);
        return authorService.createOrReturnExisting(dto); // якщо автор вже є, повертає існуючого
    }

    @Operation(summary = "Update an existing author", description = "Updates the author with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Author not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public AuthorResponseDto update(
            @Parameter(description = "ID of the author to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated author details", required = true)
            @Valid @RequestBody AuthorRequestDto dto) {
        log.info("Updating author with id {}: {}", id, dto);
        return authorService.update(id, dto);
    }

    @Operation(summary = "Delete an author", description = "Deletes the author with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Author successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID of the author to delete", required = true)
            @PathVariable Long id) {
        log.info("Deleting author with id {}", id);
        authorService.delete(id);
    }
}

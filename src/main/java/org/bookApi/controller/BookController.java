package org.bookApi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bookApi.dto.BookRequestDto;
import org.bookApi.dto.BookResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.bookApi.dto.UploadResponseDto;
import org.bookApi.service.BookService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Create a new book", description = "Creates a new book with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<BookResponseDto> create(
            @Parameter(description = "Book details for creation", required = true)
            @Valid @RequestBody BookRequestDto dto) {
        log.info("Creating book: {}", dto);
        BookResponseDto created = bookService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get all books", description = "Returns a list of all books in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = BookResponseDto.class))))
    })
    @GetMapping
    public PaginatedResponseDto<BookResponseDto> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size) {
        log.info("Fetching books, page={}, size={}", page, size);
        return bookService.getList(null, null, null, page, size);
    }

    @Operation(summary = "Get a book by ID", description = "Returns a single book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getById(
            @Parameter(description = "ID of the book to fetch", required = true)
            @PathVariable Long id) {
        log.info("Fetching book with id: {}", id);
        BookResponseDto dto = bookService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update an existing book", description = "Updates a book with the specified ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> update(
            @Parameter(description = "ID of the book to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated book details", required = true)
            @Valid @RequestBody BookRequestDto dto) {
        log.info("Updating book with id {}: {}", id, dto);
        BookResponseDto updated = bookService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a book", description = "Deletes a book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the book to delete", required = true)
            @PathVariable Long id) {
        log.info("Deleting book with id: {}", id);
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search books with filters", description = "Searches for books using optional filters and pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedResponseDto.class)))
    })
    @PostMapping("/search")
    public PaginatedResponseDto<BookResponseDto> search(
            @Parameter(description = "Filter by author ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "Filter by book title") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by year published") @RequestParam(required = false) Integer yearPublished,
            @Parameter(description = "Page number, default 1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size, default 3") @RequestParam(defaultValue = "3") int size) {
        log.info("Searching books - authorId: {}, title: {}, year: {}, page: {}, size: {}",
                authorId, title, yearPublished, page, size);
        return bookService.getList(authorId, title, yearPublished, page, size);
    }

    @Operation(summary = "Generate CSV report for books", description = "Generates a CSV report with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV report generated")
    })
    @PostMapping("/_report")
    public ResponseEntity<byte[]> report(
            @Parameter(description = "Filter by author ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "Filter by book title") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by year published") @RequestParam(required = false) Integer yearPublished) throws IOException {
        log.info("Generating CSV report");
        byte[] data = bookService.generateCsvReport(authorId, title, yearPublished);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books_report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    @Operation(summary = "Upload books from JSON file", description = "Uploads multiple books from a JSON file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or content")
    })
    @PostMapping(value = "/_upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto upload(
            @Parameter(description = "JSON file containing books", required = true)
            @RequestPart("file") MultipartFile file) throws IOException {
        log.info("Uploading books from file: {}", file.getOriginalFilename());
        return bookService.uploadFromJson(file);
    }
}

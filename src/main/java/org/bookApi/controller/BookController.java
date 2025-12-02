package org.bookApi.controller;

import lombok.RequiredArgsConstructor;
import org.bookApi.dto.BookRequestDto;
import org.bookApi.dto.BookResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.bookApi.dto.UploadResponseDto;
import org.bookApi.service.BookService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @PostMapping
    public BookResponseDto create(@Valid @RequestBody BookRequestDto dto) {
        log.info("Creating a new book: {}", dto);
        BookResponseDto createdBook = bookService.create(dto);
        log.info("Book created with id: {}", createdBook.id());
        return createdBook;
    }

    @GetMapping("/{id}")
    public BookResponseDto getById(@PathVariable Long id) {
        log.info("Fetching book with id: {}", id);
        BookResponseDto book = bookService.getById(id);
        log.info("Fetched book: {}", book);
        return book;
    }

    @PutMapping("/{id}")
    public BookResponseDto update(@PathVariable Long id, @Valid @RequestBody BookRequestDto dto) {
        log.info("Updating book with id {}: {}", id, dto);
        BookResponseDto updatedBook = bookService.update(id, dto);
        log.info("Book updated with id: {}", updatedBook.id());
        return updatedBook;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Deleting book with id: {}", id);
        bookService.delete(id);
        log.info("Book deleted with id: {}", id);
    }

    @PostMapping("/search")
    public PaginatedResponseDto<BookResponseDto> search(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer yearPublished,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("Searching books with filters - authorId: {}, title: {}, yearPublished: {}, page: {}, size: {}",
                authorId, title, yearPublished, page, size);
        PaginatedResponseDto<BookResponseDto> result = bookService.getList(authorId, title, yearPublished, page, size);
        log.info("Search result contains {} books", result.list().size());
        return result;
    }

    @PostMapping("/_report")
    public ResponseEntity<byte[]> report(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer yearPublished
    ) throws IOException {
        log.info("Generating CSV report for books with filters - authorId: {}, title: {}, yearPublished: {}",
                authorId, title, yearPublished);
        byte[] data = bookService.generateCsvReport(authorId, title, yearPublished);
        log.info("CSV report generated, size: {} bytes", data.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books_report.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    @PostMapping("/_upload")
    public UploadResponseDto upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Uploading books from file: {}", file.getOriginalFilename());
        UploadResponseDto response = bookService.uploadFromJson(file);
        log.info("Upload completed, successful: {}, failed: {}", response.successCount(), response.failedCount());
        return response;
    }
}

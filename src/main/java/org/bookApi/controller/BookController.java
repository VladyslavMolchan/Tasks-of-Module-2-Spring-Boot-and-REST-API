package org.bookApi.controller;

import lombok.RequiredArgsConstructor;
import org.bookApi.dto.BookRequestDto;
import org.bookApi.dto.BookResponseDto;
import org.bookApi.dto.PaginatedResponseDto;
import org.bookApi.dto.UploadResponseDto;
import org.bookApi.service.BookService;
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

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public BookResponseDto create(@Valid @RequestBody BookRequestDto dto) {
        return bookService.create(dto);
    }

    @PutMapping("/{id}")
    public BookResponseDto update(@PathVariable Long id, @Valid @RequestBody BookRequestDto dto) {
        return bookService.update(id, dto);
    }

    @GetMapping("/{id}")
    public BookResponseDto getById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }

    @PostMapping("/_list")
    public PaginatedResponseDto<BookResponseDto> getList(
            @RequestBody BookRequestDto dto,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return bookService.getList(dto.getAuthorId(), dto.getTitle(), dto.getYearPublished(), page, size);
    }

    @PostMapping("/_report")
    public ResponseEntity<byte[]> generateReport(@RequestBody BookRequestDto dto) throws IOException {
        byte[] file = bookService.generateCsvReport(dto.getAuthorId(), dto.getTitle(), dto.getYearPublished());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=books_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }

    @PostMapping("/_upload")
    public UploadResponseDto uploadBooks(@RequestParam("file") MultipartFile file) throws IOException {
        return bookService.uploadFromJson(file);
    }
}

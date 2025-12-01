package org.bookApi.controller;


import org.bookApi.dto.BookRequestDto;
import org.bookApi.dto.BookResponseDto;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BookRequestDto createBookDto(String title, Long authorId, int yearPublished) {
        return BookRequestDto.builder()
                .title(title)
                .authorId(authorId)
                .yearPublished(yearPublished)
                .genres(List.of("Fiction", "Adventure"))
                .build();
    }

    @Test
    void testCreateAndGetBook() throws Exception {
        BookRequestDto dto = createBookDto("Test Book", 1L, 2022);
        String json = objectMapper.writeValueAsString(dto);


        String response = mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andReturn().getResponse().getContentAsString();

        BookResponseDto created = objectMapper.readValue(response, BookResponseDto.class);


        mockMvc.perform(get("/api/book/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));


        mockMvc.perform(get("/api/book/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateBook() throws Exception {
        BookRequestDto dto = createBookDto("Old Title", 1L, 2020);
        String json = objectMapper.writeValueAsString(dto);
        String response = mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        BookResponseDto created = objectMapper.readValue(response, BookResponseDto.class);

        BookRequestDto updateDto = createBookDto("New Title", 2L, 2021);
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/book/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));


        mockMvc.perform(put("/api/book/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBook() throws Exception {
        BookRequestDto dto = createBookDto("Book to Delete", 1L, 2020);
        String json = objectMapper.writeValueAsString(dto);
        String response = mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();
        BookResponseDto created = objectMapper.readValue(response, BookResponseDto.class);

        mockMvc.perform(delete("/api/book/" + created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/book/" + created.getId()))
                .andExpect(status().isNotFound());


        mockMvc.perform(delete("/api/book/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetListBooks() throws Exception {
        BookRequestDto dto = createBookDto(null, null, 0); // пусті фільтри
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/book/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void testGenerateReport() throws Exception {
        BookRequestDto dto = createBookDto(null, null, 0);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/book/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=books_report.csv"))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    void testUploadBooks() throws Exception {
        String jsonContent = "[{\"title\":\"Upload Book\",\"authorId\":1,\"yearPublished\":2022}]";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "books.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/book/_upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(1))
                .andExpect(jsonPath("$.failedCount").value(0));


        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "invalid.json",
                MediaType.APPLICATION_JSON_VALUE,
                "[{\"title\":\"\",\"authorId\":null,\"yearPublished\":2022}]".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/book/_upload").file(invalidFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount").value(0))
                .andExpect(jsonPath("$.failedCount").value(1));
    }

    @Test
    void testValidationErrors() throws Exception {

        BookRequestDto dto = createBookDto("", 1L, 2020);
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());


        dto = createBookDto("Title", 1L, 999);
        json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());


        dto = createBookDto("Title", null, 2020);
        json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }




    @Test
    void testGetListBooksWithFilter() throws Exception {
        BookRequestDto dto1 = createBookDto("Filtered Book", 1L, 2022);
        BookRequestDto dto2 = createBookDto("Another Book", 2L, 2021);

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk());


        mockMvc.perform(post("/api/book/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":1,\"page\":1,\"size\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].title").value("Filtered Book"))
                .andExpect(jsonPath("$.list.length()").value(1));
    }

    @Test
    void testGenerateReportWithFilter() throws Exception {
        BookRequestDto dto = createBookDto("Report Book", 1L, 2023);
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/book/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":1}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=books_report.csv"))
                .andExpect(content().contentType("text/csv"));
    }

}
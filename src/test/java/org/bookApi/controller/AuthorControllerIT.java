package org.bookApi.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookApi.dto.AuthorRequestDto;
import org.bookApi.dto.AuthorResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc

class AuthorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthorRequestDto createAuthorDto(String name) {
        return AuthorRequestDto.builder().name(name).build();
    }

    @Test
    void testCreateAndGetAuthor() throws Exception {
        AuthorRequestDto dto = createAuthorDto("New Author");


        String json = objectMapper.writeValueAsString(dto);
        String response = mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Author"))
                .andReturn().getResponse().getContentAsString();

        AuthorResponseDto created = objectMapper.readValue(response, AuthorResponseDto.class);


        mockMvc.perform(get("/api/author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").isNotEmpty())
                .andExpect(jsonPath("$[*].name").isNotEmpty());


        mockMvc.perform(get("/api/author/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Author"));
    }

    @Test
    void testUpdateAuthor() throws Exception {
        AuthorRequestDto dto = createAuthorDto("Author Update");
        String json = objectMapper.writeValueAsString(dto);
        String response = mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        AuthorResponseDto created = objectMapper.readValue(response, AuthorResponseDto.class);


        AuthorRequestDto updateDto = createAuthorDto("Updated Name");
        String updateJson = objectMapper.writeValueAsString(updateDto);
        mockMvc.perform(put("/api/author/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void testDeleteAuthor() throws Exception {
        AuthorRequestDto dto = createAuthorDto("Author Delete");
        String json = objectMapper.writeValueAsString(dto);
        String response = mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        AuthorResponseDto created = objectMapper.readValue(response, AuthorResponseDto.class);


        mockMvc.perform(delete("/api/author/" + created.getId()))
                .andExpect(status().isOk());


        mockMvc.perform(get("/api/author/" + created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationAndDuplicate() throws Exception {

        AuthorRequestDto dto = createAuthorDto("");
        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());


        AuthorRequestDto dto2 = createAuthorDto("Unique Author");
        String json2 = objectMapper.writeValueAsString(dto2);
        mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isOk());


        mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json2))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetNonExistingAuthor() throws Exception {

        mockMvc.perform(get("/api/author/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateAuthorWithBlankName() throws Exception {

        AuthorRequestDto dto = createAuthorDto("Author To Update Blank");
        String json = objectMapper.writeValueAsString(dto);
        String response = mockMvc.perform(post("/api/author")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        AuthorResponseDto created = objectMapper.readValue(response, AuthorResponseDto.class);


        AuthorRequestDto updateDto = createAuthorDto("");
        String updateJson = objectMapper.writeValueAsString(updateDto);
        mockMvc.perform(put("/api/author/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isBadRequest());
    }

}

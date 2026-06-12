package ru.practicum.explorewithme.service.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.service.compilation.service.CompilationService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompilationPublicController.class)
class CompilationPublicControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCompilations_ShouldReturn200() throws Exception {
        CompilationDto compilation = CompilationDto.builder().id(1L).title("Test").pinned(true).events(List.of()).build();

        Mockito.when(compilationService.getAll(Mockito.nullable(Boolean.class), Mockito.eq(0), Mockito.eq(10))).thenReturn(List.of(compilation));

        mockMvc.perform(get("/compilations")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getCompilation_ShouldReturn200() throws Exception {
        CompilationDto compilation = CompilationDto.builder().id(1L).title("Test").pinned(true).events(List.of()).build();

        Mockito.when(compilationService.getById(1L)).thenReturn(compilation);

        mockMvc.perform(get("/compilations/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }
}
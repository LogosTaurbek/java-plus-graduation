package ru.practicum.explorewithme.service.compilation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.explorewithme.service.compilation.service.CompilationService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompilationAdminController.class)
class CompilationAdminControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCompilation_ShouldReturn201() throws Exception {
        NewCompilationDto request = new NewCompilationDto("Test", false, List.of());
        CompilationDto response = CompilationDto.builder().id(1L).title("Test").pinned(false).events(List.of()).build();

        Mockito.when(compilationService.create(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/admin/compilations").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateCompilation_ShouldReturn200() throws Exception {
        UpdateCompilationRequestDto request = new UpdateCompilationRequestDto("Updated", true, List.of());
        CompilationDto response = CompilationDto.builder().id(1L).title("Updated").pinned(true).events(List.of()).build();

        Mockito.when(compilationService.update(Mockito.eq(1L), Mockito.any())).thenReturn(response);

        mockMvc.perform(patch("/admin/compilations/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deleteCompilation_ShouldReturn204() throws Exception {
        Mockito.doNothing().when(compilationService).delete(1L);

        mockMvc.perform(delete("/admin/compilations/1")).andExpect(status().isNoContent());
    }
}
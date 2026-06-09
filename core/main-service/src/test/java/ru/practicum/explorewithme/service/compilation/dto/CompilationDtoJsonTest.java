package ru.practicum.explorewithme.service.compilation.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationDtoJsonTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serialize_ShouldReturnJson() throws Exception {
        CompilationDto dto = CompilationDto.builder().id(1L).title("Test").pinned(true).events(List.of()).build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"title\":\"Test\"");
    }

    @Test
    void deserialize_ShouldReturnDto() throws Exception {
        String json = "{\"id\":1,\"title\":\"Test\",\"pinned\":true,\"events\":[]}";

        CompilationDto dto = objectMapper.readValue(json, CompilationDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Test");
        assertThat(dto.getPinned()).isTrue();
    }
}
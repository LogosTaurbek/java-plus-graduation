package ru.practicum.explorewithme.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;
import ru.practicum.explorewithme.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    @MockBean
    private StatsService statsService;

    private EndpointHitDTO validHit;
    private ViewStatsDTO statsEntry;

    @BeforeEach
    void setUp() {
        validHit = new EndpointHitDTO(null, "ewm-main-service", "/events", "121.0.0.1",
                LocalDateTime.of(2025, 5, 5, 12, 0, 0));
        statsEntry = new ViewStatsDTO("ewm-main-service", "/events", 5L);
    }

    // --- POST /hit ---
    @Test
    void hit_Success() throws Exception {
        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validHit)))
                .andExpect(status().isCreated());

        verify(statsService).hit(any(EndpointHitDTO.class));
    }

    @Test
    void hit_ValidationFail_ShouldReturn400() throws Exception {
        // Отсутствует поле "app"
        // CHECKSTYLE:OFF
        String json = """
                {
                    "uri": "/events",
                    "ip": "121.0.0.1",
                    "timestamp": "2025-05-05 12:00:00"
                }
                """;
        // CHECKSTYLE:ON

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
        // сервис не должен вызываться
        verify(statsService, never()).hit(any());
    }

    @Test
    void hit_InvalidTimestampFormat_ShouldReturn400() throws Exception {
        // CHECKSTYLE:OFF
        String json = """
                {
                    "app": "app",
                    "uri": "/test",
                    "ip": "1.1.1.1",
                    "timestamp": "12-05-2025"
                }
                """;
        // CHECKSTYLE:ON

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // --- GET /stats ---
    @Test
    void getStats_WithoutOptionalParams_Success() throws Exception {
        when(statsService.getStats(any(), any(), eq(null), eq(null)))
                .thenReturn(List.of(statsEntry));

        mockMvc.perform(get("/stats")
                        .param("start", "2020-05-05 00:00:00")
                        .param("end", "2030-05-05 00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events"))
                .andExpect(jsonPath("$[0].hits").value(5));
    }

    @Test
    void getStats_WithUrisAndUnique_Success() throws Exception {
        when(statsService.getStats(any(), any(), eq(List.of("/events", "/events/5")), eq(true)))
                .thenReturn(List.of(statsEntry));

        mockMvc.perform(get("/stats")
                        .param("start", "2020-05-05 00:00:00")
                        .param("end", "2030-05-05 00:00:00")
                        .param("uris", "/events", "/events/5")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(statsService).getStats(any(), any(), eq(List.of("/events", "/events/5")), eq(true));
    }

    @Test
    void getStats_MissingStart_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", "2030-05-05 00:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_InvalidDateFormat_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "05-05-2020")
                        .param("end", "05-05-2030"))
                .andExpect(status().isBadRequest());
    }
}
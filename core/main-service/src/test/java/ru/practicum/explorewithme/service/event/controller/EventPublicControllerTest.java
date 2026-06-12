package ru.practicum.explorewithme.service.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.config.AppProperties;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.stats.client.StatsClient;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventPublicController.class)
public class EventPublicControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EventService eventService;

    @MockBean
    private StatsClient statsClient;

    @MockBean
    private AppProperties appProperties;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldReturnEventsWithParams() throws Exception {

        List<EventShortDto> events = List.of(new EventShortDto());

        when(eventService.getEventsPublic(any()))
                .thenReturn(events);

        mockMvc.perform(get("/events")
                        .param("text", "concert")
                        .param("categories", "1", "2")
                        .param("paid", "true")
                        .param("rangeStart", "2025-01-01 10:00:00")
                        .param("rangeEnd", "2025-01-02 10:00:00")
                        .param("onlyAvailable", "true")
                        .param("sort", "VIEWS")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(eventService, times(1)).getEventsPublic(any());
    }

    @Test
    void shouldReturn400WhenEndBeforeStart() throws Exception {

        mockMvc.perform(get("/events")
                        .param("rangeStart", "2025-01-02 10:00:00")
                        .param("rangeEnd", "2025-01-01 10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEventWithViews() throws Exception {

        Long eventId = 1L;

        EventFullDto event = EventFullDto.builder()
                .id(eventId)
                .createdOn("2025-01-01 10:00:00")
                .views(5L)
                .build();

        when(appProperties.getName())
                .thenReturn("ewm-main-service");
        when(eventService.getEventPublic(eventId)).thenReturn(event);

        mockMvc.perform(get("/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.views").value(5));


        verify(statsClient, times(1)).saveHit(any());
        verify(statsClient, never()).getStats(any(), any(), any(), any());
    }

}

// EventControllerTest.java
package ru.practicum.explorewithme.service.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.ErrorHandler;
import ru.practicum.explorewithme.service.exception.NotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest({EventPrivateController.class, EventAdminController.class})
@Import(ErrorHandler.class)
class EventControllerTest {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    @MockBean
    private EventService eventService;

    private NewEventDto validNewEvent;
    private EventFullDto fullDto;
    private EventShortDto shortDto;
    private UpdateEventUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        validNewEvent = NewEventDto.builder()
                .annotation("Valid annotation for testing")
                .category(1L)
                .description("Valid description for testing")
                .eventDate("2030-12-31 15:10:05")
                .location(new LocationDto(55.75f, 37.62f))
                .paid(false)
                .participantLimit(0)
                .requestModeration(true)
                .title("Valid title")
                .build();

        fullDto = EventFullDto.builder()
                .id(1L)
                .annotation("Valid annotation")
                .eventDate("2030-12-31 15:10:05")
                .state(EventState.PENDING)
                .build();

        shortDto = EventShortDto.builder()
                .id(1L)
                .annotation("Valid annotation")
                .eventDate("2030-12-31 15:10:05")
                .build();

        updateRequest = UpdateEventUserRequest.builder()
                .title("Updated title")
                .build();
    }

    // POST /users/{userId}/events
    @Test
    void addEvent_Success() throws Exception {
        when(eventService.addEvent(anyLong(), any(NewEventDto.class))).thenReturn(fullDto);

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNewEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(eventService).addEvent(1L, validNewEvent);
    }

    @Test
    void addEvent_ValidationFail_ShouldReturn400() throws Exception {
        String json = "{\n"
                + "    \"annotation\": \"aa\",\n"
                + "    \"category\": 1,\n"
                + "    \"description\": \"Valid description for testing\",\n"
                + "    \"eventDate\": \"2030-12-31 15:10:05\",\n"
                + "    \"location\": { \"lat\": 55.75, \"lon\": 37.62 },\n"
                + "    \"title\": \"Valid title\"\n"
                + "}";

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).addEvent(anyLong(), any());
    }

    @Test
    void addEvent_DateConflict_ShouldReturn409() throws Exception {
        when(eventService.addEvent(anyLong(), any(NewEventDto.class)))
                .thenThrow(new ConflictException("Дата события должна быть не ранее чем через 2 часа от текущего момента"));

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validNewEvent)))
                .andExpect(status().isConflict());

        verify(eventService).addEvent(1L, validNewEvent);
    }

    // GET /users/{userId}/events
    @Test
    void getEvents_Success() throws Exception {
        when(eventService.getEvents(1L, 0, 10)).thenReturn(List.of(shortDto));

        mockMvc.perform(get("/users/{userId}/events", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getEvents_DefaultPagination() throws Exception {
        when(eventService.getEvents(1L, 0, 10)).thenReturn(List.of());

        mockMvc.perform(get("/users/{userId}/events", 1L))
                .andExpect(status().isOk());

        verify(eventService).getEvents(1L, 0, 10);
    }

    // GET /users/{userId}/events/{eventId}
    @Test
    void getEvent_Success() throws Exception {
        when(eventService.getEvent(1L, 2L)).thenReturn(fullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getEvent_NotFound_ShouldReturn404() throws Exception {
        when(eventService.getEvent(1L, 999L))
                .thenThrow(new NotFoundException("Событие с id=999 не найдено"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    // PATCH /users/{userId}/events/{eventId}
    @Test
    void updateEvent_Success() throws Exception {
        when(eventService.updateEvent(eq(1L), eq(2L), any(UpdateEventUserRequest.class)))
                .thenReturn(fullDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void updateEvent_PublishedConflict_ShouldReturn409() throws Exception {
        when(eventService.updateEvent(eq(1L), eq(2L), any(UpdateEventUserRequest.class)))
                .thenThrow(new ConflictException("Нельзя редактировать опубликованное событие"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }
}

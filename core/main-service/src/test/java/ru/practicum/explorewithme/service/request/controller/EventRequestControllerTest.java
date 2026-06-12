// EventRequestControllerTest.java
package ru.practicum.explorewithme.service.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.ErrorHandler;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.service.EventRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventRequestPrivateController.class)
@Import(ErrorHandler.class)
class EventRequestControllerTest {

    @SuppressWarnings("unused")
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("unused")
    @MockBean
    private EventRequestService eventRequestService;

    private ParticipationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = ParticipationRequestDto.builder()
                .id(1L)
                .requester(2L)
                .event(3L)
                .status(ParticipationRequestStatus.PENDING)
                .created(java.time.LocalDateTime.now())
                .build();
    }

    @Test
    void getEventRequests_Success() throws Exception {
        when(eventRequestService.getEventRequests(1L, 2L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getEventRequests_EventNotFound_ShouldReturn404() throws Exception {
        when(eventRequestService.getEventRequests(1L, 999L))
                .thenThrow(new NotFoundException("Событие не найдено"));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEventRequests_Success() throws Exception {
        EventRequestStatusUpdateRequest updateReq = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();
        EventRequestStatusUpdateResult result = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(List.of(requestDto))
                .rejectedRequests(List.of())
                .build();
        when(eventRequestService.updateEventRequests(eq(1L), eq(2L), any())).thenReturn(result);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(1));
    }

    @Test
    void updateEventRequests_Conflict_ShouldReturn409() throws Exception {
        EventRequestStatusUpdateRequest updateReq = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();
        when(eventRequestService.updateEventRequests(eq(1L), eq(2L), any()))
                .thenThrow(new ConflictException("Лимит достигнут"));

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isConflict());
    }
}

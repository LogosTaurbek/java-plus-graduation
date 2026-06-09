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
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParamsAdmin;
import ru.practicum.explorewithme.service.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.event.enums.AdminEventStateAction;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.ErrorHandler;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventAdminController.class)
@Import(ErrorHandler.class)
class EventAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventFullDto fullDto;
    private UpdateEventAdminRequest updateRequest;

    @BeforeEach
    void setUp() {
        fullDto = EventFullDto.builder()
                .id(1L)
                .annotation("Admin annotation")
                .eventDate("2030-12-31 15:10:05")
                .state(EventState.PUBLISHED)
                .build();

        updateRequest = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                .build();
    }

    @Test
    void getEvents_Success() throws Exception {
        when(eventService.getEventsByAdmin(any(EventSearchParamsAdmin.class)))
                .thenReturn(List.of(fullDto));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1,2")
                        .param("states", "PUBLISHED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService).getEventsByAdmin(any(EventSearchParamsAdmin.class));
    }

    @Test
    void updateEventByAdmin_Success() throws Exception {
        UpdateEventAdminRequest adminRequest = new UpdateEventAdminRequest();
        adminRequest.setTitle("New Admin Title");

        when(eventService.updateEventByAdmin(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenReturn(fullDto);

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateEvent_Conflict_ShouldReturn409() throws Exception {
        when(eventService.updateEventByAdmin(eq(1L), any(UpdateEventAdminRequest.class)))
                .thenThrow(new ConflictException("Событие не удовлетворяет правилам редактирования"));

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }
}

package ru.practicum.explorewithme.service.location.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.service.LocationService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationAdminController.class)
public class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationService locationService;

    @MockBean
    private EventService eventService;

    private NewLocationRequest request;
    private LocationDto location;
    private UpdateLocationRequest updateRequest;
    private LocationDto updatedLocation;
    Long locId;

    @BeforeEach
    void setUp() {
        locId = 1L;

        request = NewLocationRequest.builder()
                .name("Moscow")
                .lat(55.7558f)
                .lon(37.6173f)
                .radius(300.00f)
                .build();

        location = LocationDto.builder()
                .name("Moscow")
                .id(1L)
                .lat(55.7558f)
                .lon(37.6173f)
                .radius(300.00f)
                .build();

        updateRequest = UpdateLocationRequest.builder()
                .name("Moscva")
                .lat(55.7560f)
                .lon(37.6175f)
                .radius(350.00f)
                .build();

        updatedLocation = LocationDto.builder()
                .name("Moscva")
                .lat(55.7560f)
                .lon(37.6175f)
                .radius(350.00f)
                .build();
    }

    @Test
    void shouldSaveNewLocation() throws Exception {
        Mockito.when(locationService.createLocation(request))
                .thenReturn(location);

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lat").value(55.7558f))
                .andExpect(jsonPath("$.lon").value(37.6173f))
                .andExpect(jsonPath("$.radius").value(300.00f))
                .andExpect(jsonPath("$.name").value("Moscow"));

        verify(locationService).createLocation(any(NewLocationRequest.class));
    }

    @Test
    void shouldUpdateLocation() throws Exception {
        Mockito.when(locationService.updateLocation(locId, updateRequest))
                .thenReturn(updatedLocation);

        mockMvc.perform(patch("/admin/locations/{locId}", locId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lat").value(55.7560f))
                .andExpect(jsonPath("$.lon").value(37.6175f))
                .andExpect(jsonPath("$.radius").value(350.00f))
                .andExpect(jsonPath("$.name").value("Moscva"));
    }

    @Test
    void shouldDeleteLocation() throws Exception {
        doNothing().when(locationService).deleteLocation(locId);

        mockMvc.perform(delete("/admin/locations/{locId}", locId))
                .andExpect(status().isNoContent());

        verify(locationService).deleteLocation(locId);
    }

    @Test
    void shouldThrowExceptionWhenLocationNotFound() throws Exception {
        doThrow(new NotFoundException("Локация" + locId + " не найдена"))
                .when(locationService).deleteLocation(locId);

        mockMvc.perform(delete("/admin/locations/{lockId}", locId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("Требуемый объект не найден"));
    }

    @Test
    void shouldReturnEventList_whenLocationExists() throws Exception {
        EventFullDto dto = EventFullDto.builder().id(10L).title("Test Event").build();
        when(eventService.getEventsByLocation(eq(locId), eq(0), eq(10))).thenReturn(List.of(dto));

        mockMvc.perform(get("/admin/locations/{locId}/events", locId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].title").value("Test Event"));

        verify(eventService).getEventsByLocation(locId, 0, 10);
    }

    @Test
    void shouldReturn404_whenLocationNotFound() throws Exception {
        when(eventService.getEventsByLocation(eq(locId), eq(0), eq(10)))
                .thenThrow(new NotFoundException("Локация " + locId + " не найдена"));

        mockMvc.perform(get("/admin/locations/{locId}/events", locId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("Требуемый объект не найден"));
    }

    @Test
    void shouldReturn200WithEmptyList_whenNoEvents() throws Exception {
        when(eventService.getEventsByLocation(eq(locId), eq(0), eq(10))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/locations/{locId}/events", locId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldUseDefaultPagination_whenFromAndSizeOmitted() throws Exception {
        when(eventService.getEventsByLocation(any(), eq(0), eq(10))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/locations/{locId}/events", locId))
                .andExpect(status().isOk());

        verify(eventService).getEventsByLocation(locId, 0, 10);
    }
}

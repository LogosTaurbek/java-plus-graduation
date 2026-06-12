package ru.practicum.explorewithme.service.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParamsAdmin;
import ru.practicum.explorewithme.service.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<EventState> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        EventSearchParamsAdmin params = EventSearchParamsAdmin.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        return eventService.getEventsByAdmin(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventByAdmin(eventId, request);
    }
}

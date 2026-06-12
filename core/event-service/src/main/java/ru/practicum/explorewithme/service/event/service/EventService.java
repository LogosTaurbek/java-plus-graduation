package ru.practicum.explorewithme.service.event.service;

import ru.practicum.explorewithme.service.event.dto.*;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEvents(Long userId, int from, int size);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventShortDto> getEventsPublic(EventSearchParams params);

    EventFullDto getEventPublic(Long eventId);

    List<EventFullDto> getEventsByAdmin(EventSearchParamsAdmin params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventFullDto> getEventsByLocation(Long locId, int from, int size);
}

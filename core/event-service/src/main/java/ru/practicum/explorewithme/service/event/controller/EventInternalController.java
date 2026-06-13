package ru.practicum.explorewithme.service.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.event.client.UserClient;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.EventForRequestDto;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.mapper.EventMapper;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class EventInternalController {

    private final EventRepository eventRepository;
    private final UserClient userClient;

    @GetMapping("/{eventId}")
    public EventForRequestDto getEventById(@PathVariable Long eventId) {
        return eventRepository.findById(eventId)
                .map(e -> new EventForRequestDto(
                        e.getId(),
                        e.getInitiatorId(),
                        e.getState().name(),
                        e.getParticipantLimit(),
                        e.getRequestModeration()))
                .orElseThrow(() -> new NotFoundException("Событие " + eventId + " не найдено"));
    }

    @GetMapping
    public List<EventShortDto> getEventsByIds(@RequestParam List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<Event> events = eventRepository.findAllById(ids);
        List<Long> initiatorIds = events.stream().map(Event::getInitiatorId).distinct().collect(Collectors.toList());
        Map<Long, UserShortDto> userMap = userClient.getUsersByIds(initiatorIds).stream()
                .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        return events.stream()
                .map(e -> EventMapper.toShortDto(
                        e,
                        userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                        0L, 0.0))
                .collect(Collectors.toList());
    }
}

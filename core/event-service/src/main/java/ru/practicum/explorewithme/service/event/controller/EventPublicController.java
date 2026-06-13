package ru.practicum.explorewithme.service.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.proto.collector.ActionTypeProto;
import ru.practicum.explorewithme.service.event.client.ParticipationRequestDto;
import ru.practicum.explorewithme.service.event.client.RequestClient;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParams;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.exception.BadRequestException;
import ru.practicum.explorewithme.service.util.EwmHttpHeaders;
import ru.practicum.explorewithme.stats.client.CollectorClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventPublicController {
    private final EventService eventService;
    private final CollectorClient collectorClient;
    private final RequestClient requestClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(required = false) Boolean onlyAvailable,
                                         @RequestParam(required = false) String sort,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size) {
        if (rangeEnd != null && rangeStart != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("rangeEnd должен быть позже rangeStart");
        }

        EventSearchParams params = EventSearchParams.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        return eventService.getEventsPublic(params);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdAndPublished(@PathVariable(name = "eventId") Long eventId,
                                                 @RequestHeader(value = EwmHttpHeaders.USER_ID, required = false, defaultValue = "0") long userId) {
        log.info("Запрос на получение события {}", eventId);

        if (userId != 0) {
            collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW, Instant.now());
        }

        return eventService.getEventPublic(eventId);
    }

    @GetMapping("/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getRecommendations(
            @RequestHeader(value = EwmHttpHeaders.USER_ID, required = false, defaultValue = "0") long userId) {
        if (userId == 0) return Collections.emptyList();
        return eventService.getRecommendedEvents(userId);
    }

    @GetMapping("/{eventId}/similar")
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getSimilarEvents(@PathVariable Long eventId,
                                                @RequestHeader(value = EwmHttpHeaders.USER_ID, required = false, defaultValue = "0") long userId,
                                                @RequestParam(defaultValue = "10") int maxResults) {
        return eventService.getSimilarEvents(eventId, userId, maxResults);
    }

    @PutMapping("/{eventId}/like")
    @ResponseStatus(HttpStatus.OK)
    public void likeEvent(@PathVariable Long eventId,
                          @RequestHeader(value = EwmHttpHeaders.USER_ID) long userId) {
        List<ParticipationRequestDto> userRequests = requestClient.getUserRequests(userId);
        boolean hasConfirmed = userRequests.stream()
                .anyMatch(r -> r.getEvent().equals(eventId) && "CONFIRMED".equals(r.getStatus()));

        if (!hasConfirmed) {
            throw new BadRequestException("У пользователя нет подтверждённой заявки на событие " + eventId);
        }

        collectorClient.collectUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
    }
}

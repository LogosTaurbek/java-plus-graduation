package ru.practicum.explorewithme.service.event.mapper;

import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EventMapper {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event toEntity(NewEventDto dto) {
        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setDescription(dto.getDescription());
        event.setEventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER));
        event.setLocation(new ru.practicum.explorewithme.service.event.model.Location(
                dto.getLocation().getLat(), dto.getLocation().getLon()));
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setTitle(dto.getTitle());
        event.setState(ru.practicum.explorewithme.service.event.enums.EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        return event;
    }

    public static EventFullDto toFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null)
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views != null ? views : 0L)
                .build();
    }

    public static EventFullDto toFullDto(Event event) {
        return toFullDto(event, 0L, 0L);
    }

    public static EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests != null ? confirmedRequests : 0L)
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views != null ? views : 0L)
                .build();
    }

    public static EventShortDto toShortDto(Event event) {
        return toShortDto(event, 0L, 0L);
    }

    public static void updateEntityFromRequest(UpdateEventUserRequest request, Event event) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(LocalDateTime.parse(request.getEventDate(), FORMATTER));
        if (request.getLocation() != null)
            event.setLocation(new ru.practicum.explorewithme.service.event.model.Location(
                    request.getLocation().getLat(), request.getLocation().getLon()));
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
    }

    public static void updateEntityFromAdminRequest(UpdateEventAdminRequest request, Event event) {
        if (request.getAnnotation() != null) event.setAnnotation(request.getAnnotation());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventDate() != null) event.setEventDate(LocalDateTime.parse(request.getEventDate(), FORMATTER));
        if (request.getLocation() != null)
            event.setLocation(new ru.practicum.explorewithme.service.event.model.Location(
                    request.getLocation().getLat(), request.getLocation().getLon()));
        if (request.getPaid() != null) event.setPaid(request.getPaid());
        if (request.getParticipantLimit() != null) event.setParticipantLimit(request.getParticipantLimit());
        if (request.getRequestModeration() != null) event.setRequestModeration(request.getRequestModeration());
        if (request.getTitle() != null) event.setTitle(request.getTitle());
    }
}

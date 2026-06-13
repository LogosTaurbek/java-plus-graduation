package ru.practicum.explorewithme.service.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.event.client.RequestClient;
import ru.practicum.explorewithme.service.event.client.UserClient;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.*;
import ru.practicum.explorewithme.service.event.enums.AdminEventStateAction;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.enums.UserEventStateAction;
import ru.practicum.explorewithme.service.event.mapper.EventMapper;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.event.service.predicate.EventPredicate;
import ru.practicum.explorewithme.service.exception.BadRequestException;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.location.dal.LocationRepository;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;
import ru.practicum.explorewithme.stats.client.AnalyzerClient;
import ru.practicum.ewm.stats.proto.dashboard.RecommendedEventProto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestClient requestClient;
    private final AnalyzerClient analyzerClient;

    @Value("${recommendations.max-results}")
    private int maxResults;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем id={}", userId);
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(), EventMapper.FORMATTER);
        if (ChronoUnit.HOURS.between(LocalDateTime.now(), eventDate) < 2) {
            throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
        }

        UserShortDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        Event event = EventMapper.toEntity(newEventDto);
        event.setInitiatorId(userId);
        event.setCategory(categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена")));
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);
        log.debug("Событие сохранено с id={}", event.getId());
        return EventMapper.toFullDto(event, user, 0L, 0.0);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        log.info("Получение событий пользователя id={}, from={}, size={}", userId, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable).getContent();
        Map<Long, UserShortDto> userMap = getUserMap(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        return events.stream()
                .map(e -> EventMapper.toShortDto(e,
                        userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                        confirmedRequests.getOrDefault(e.getId(), 0L), 0.0))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEvent(Long userId, Long eventId) {
        log.info("Получение события id={} пользователя id={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно"));
        long confirmed = getConfirmedRequests(List.of(event)).getOrDefault(event.getId(), 0L);
        return EventMapper.toFullDto(event, getInitiator(event.getInitiatorId()), confirmed, 0.0);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        log.info("Обновление события id={} пользователем id={}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно"));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя редактировать опубликованное событие");
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction() == UserEventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            } else if (request.getStateAction() == UserEventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }

        if (request.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(request.getEventDate(), EventMapper.FORMATTER);
            if (ChronoUnit.HOURS.between(LocalDateTime.now(), newDate) < 2) {
                throw new BadRequestException("Дата события должна быть не ранее чем через 2 часа от текущего момента");
            }
        }

        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.getCategory() + " не найдена")));
        }

        EventMapper.updateEntityFromRequest(request, event);
        eventRepository.save(event);
        log.debug("Событие обновлено");
        return EventMapper.toFullDto(event, getInitiator(event.getInitiatorId()), 0L, 0.0);
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(EventSearchParamsAdmin params) {
        log.info("Получение событий администратором");
        BooleanExpression predicate = EventPredicate.buildAdmin(params);
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(),
                Sort.by("id").ascending());
        Page<Event> events = eventRepository.findAll(predicate, pageable);
        Map<Long, UserShortDto> userMap = getUserMap(events.getContent());
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events.getContent());
        Map<Long, Double> ratings = getRatingsMap(events.getContent().stream().map(Event::getId).collect(Collectors.toList()));

        return events.stream()
                .map(e -> EventMapper.toFullDto(e,
                        userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        ratings.getOrDefault(e.getId(), 0.0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        log.info("Обновление события id={} администратором", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (request.getStateAction() != null) {
            if (request.getStateAction() == AdminEventStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
                }
                LocalDateTime now = LocalDateTime.now();
                if (event.getEventDate().isBefore(now.plusHours(1))) {
                    throw new ConflictException("Дата начала события должна быть не ранее чем за час от даты публикации");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(now);
            } else if (request.getStateAction() == AdminEventStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано");
                }
                event.setState(EventState.CANCELED);
            }
        }

        if (request.getEventDate() != null) {
            LocalDateTime newDate = LocalDateTime.parse(request.getEventDate(), EventMapper.FORMATTER);
            if (newDate.isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Дата события не может быть в прошлом");
            }
        }

        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + request.getCategory() + " не найдена")));
        }

        EventMapper.updateEntityFromAdminRequest(request, event);
        eventRepository.save(event);
        log.debug("Событие обновлено администратором");
        return EventMapper.toFullDto(event, getInitiator(event.getInitiatorId()), 0L, 0.0);
    }

    @Override
    public List<EventFullDto> getEventsByLocation(Long locId, int from, int size) {
        locationRepository.findById(locId)
                .orElseThrow(() -> new NotFoundException("Локация " + locId + " не найдена"));

        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> page = eventRepository.findEventsByLocation(locId, pageable);

        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = page.getContent();
        Map<Long, UserShortDto> userMap = getUserMap(events);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        Map<Long, Double> ratings = getRatingsMap(events.stream().map(Event::getId).collect(Collectors.toList()));

        return events.stream()
                .map(e -> EventMapper.toFullDto(e,
                        userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        ratings.getOrDefault(e.getId(), 0.0)))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> getEventsPublic(EventSearchParams params) {
        BooleanExpression predicate = EventPredicate.build(params);
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(),
                getSort(params.getSort()));

        Page<Event> page = eventRepository.findAll(predicate, pageable);
        if (page.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = page.getContent();
        Map<Long, UserShortDto> userMap = getUserMap(events);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, Double> ratings = getRatingsMap(eventIds);
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);

        List<EventShortDto> list = events.stream()
                .map(e -> EventMapper.toShortDto(e,
                        userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                        confirmedRequests.getOrDefault(e.getId(), 0L),
                        ratings.getOrDefault(e.getId(), 0.0)))
                .toList();

        if ("VIEWS".equalsIgnoreCase(params.getSort())) {
            list = list.stream()
                    .sorted(Comparator.comparingDouble(EventShortDto::getRating).reversed())
                    .toList();
        }

        log.info("Список событий после фильтрации: {} шт", list.size());
        return list;
    }

    @Override
    public EventFullDto getEventPublic(Long eventId) {
        Event e = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие " + eventId + " не найдено"));

        EventFullDto event = EventMapper.toFullDto(e, getInitiator(e.getInitiatorId()), 0L, 0.0);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие должно быть опубликовано");
        }

        Map<Long, Double> ratings = getRatingsMap(List.of(eventId));
        event.setRating(ratings.getOrDefault(eventId, 0.0));
        event.setConfirmedRequests(getConfirmedRequests(List.of(e)).getOrDefault(eventId, 0L));

        return event;
    }

    @Override
    public List<EventShortDto> getRecommendedEvents(long userId) {
        if (userId == 0) return Collections.emptyList();

        try {
            List<Long> eventIds = analyzerClient.getRecommendations(userId, maxResults)
                    .map(RecommendedEventProto::getEventId)
                    .toList();

            if (eventIds.isEmpty()) return Collections.emptyList();

            List<Event> events = eventRepository.findAllById(eventIds).stream()
                    .filter(e -> e.getState() == EventState.PUBLISHED)
                    .toList();

            if (events.isEmpty()) return Collections.emptyList();

            Map<Long, UserShortDto> userMap = getUserMap(events);
            Map<Long, Integer> order = new HashMap<>();
            for (int i = 0; i < eventIds.size(); i++) {
                order.put(eventIds.get(i), i);
            }

            return events.stream()
                    .sorted(Comparator.comparingInt(e -> order.getOrDefault(e.getId(), Integer.MAX_VALUE)))
                    .map(e -> EventMapper.toShortDto(e,
                            userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                            0L, 0.0))
                    .toList();
        } catch (Exception e) {
            log.error("Ошибка при получении рекомендаций для user {}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<EventShortDto> getSimilarEvents(long eventId, long userId, int maxResultsParam) {
        try {
            List<Long> eventIds = analyzerClient.getSimilarEvents(eventId, userId, maxResultsParam)
                    .map(RecommendedEventProto::getEventId)
                    .toList();

            if (eventIds.isEmpty()) return Collections.emptyList();

            List<Event> events = eventRepository.findAllById(eventIds).stream()
                    .filter(e -> e.getState() == EventState.PUBLISHED)
                    .toList();

            if (events.isEmpty()) return Collections.emptyList();

            Map<Long, UserShortDto> userMap = getUserMap(events);
            Map<Long, Integer> order = new HashMap<>();
            for (int i = 0; i < eventIds.size(); i++) {
                order.put(eventIds.get(i), i);
            }

            return events.stream()
                    .sorted(Comparator.comparingInt(e -> order.getOrDefault(e.getId(), Integer.MAX_VALUE)))
                    .map(e -> EventMapper.toShortDto(e,
                            userMap.getOrDefault(e.getInitiatorId(), new UserShortDto(e.getInitiatorId(), "N/A")),
                            0L, 0.0))
                    .toList();
        } catch (Exception e) {
            log.error("Ошибка при получении похожих событий для event {}: {}", eventId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<Long, Double> getRatingsMap(List<Long> eventIds) {
        if (eventIds.isEmpty()) return Collections.emptyMap();
        try {
            return analyzerClient.getInteractionsCount(eventIds)
                    .collect(Collectors.toMap(RecommendedEventProto::getEventId, RecommendedEventProto::getScore));
        } catch (Exception e) {
            log.error("Ошибка при получении рейтингов из Analyzer: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, Long> getConfirmedRequests(List<Event> events) {
        if (events.isEmpty()) return Collections.emptyMap();
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        return requestClient.getConfirmedRequestsCounts(eventIds);
    }

    private Sort getSort(String sort) {
        return Sort.by(Sort.Direction.ASC, "eventDate");
    }

    private UserShortDto getInitiator(Long initiatorId) {
        UserShortDto user = userClient.getUserById(initiatorId);
        return user != null ? user : new UserShortDto(initiatorId, "N/A");
    }

    private Map<Long, UserShortDto> getUserMap(List<Event> events) {
        List<Long> ids = events.stream().map(Event::getInitiatorId).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) return Collections.emptyMap();
        return userClient.getUsersByIds(ids).stream()
                .collect(Collectors.toMap(UserShortDto::getId, u -> u));
    }
}

package ru.practicum.explorewithme.service.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.client.EventClient;
import ru.practicum.explorewithme.service.request.client.UserClient;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.dto.ConfirmedRequestsCount;
import ru.practicum.explorewithme.service.request.dto.EventForRequestDto;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRequestRepository eventRequestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        log.info("Получение заявок на событие id={} пользователя id={}", eventId, userId);
        EventForRequestDto event = eventClient.getEventById(eventId);
        if (!userId.equals(event.getInitiatorId())) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно");
        }
        return eventRequestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса заявок на событие id={} пользователем id={}", eventId, userId);

        EventForRequestDto event = getEventAndValidateOwnership(userId, eventId);
        validateRequestPrerequisites(event);

        ParticipationRequestStatus newStatus = validateNewStatus(request.getStatus());
        List<ParticipationRequest> pendingRequests = getPendingRequestsOrThrow(request.getRequestIds());

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        if (newStatus == ParticipationRequestStatus.CONFIRMED) {
            processConfirmation(event, pendingRequests, confirmed, rejected);
        } else {
            rejectAll(pendingRequests, rejected);
        }

        eventRequestRepository.saveAll(pendingRequests);

        return buildResult(confirmed, rejected);
    }

    private EventForRequestDto getEventAndValidateOwnership(Long userId, Long eventId) {
        EventForRequestDto event = eventClient.getEventById(eventId);
        if (!userId.equals(event.getInitiatorId())) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено или недоступно");
        }
        return event;
    }

    private void validateRequestPrerequisites(EventForRequestDto event) {
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Подтверждение заявок не требуется для данного события");
        }
    }

    private ParticipationRequestStatus validateNewStatus(ParticipationRequestStatus status) {
        if (status != ParticipationRequestStatus.CONFIRMED && status != ParticipationRequestStatus.REJECTED) {
            throw new ConflictException("Неверный статус заявки");
        }
        return status;
    }

    private List<ParticipationRequest> getPendingRequestsOrThrow(List<Long> requestIds) {
        List<ParticipationRequest> requests = eventRequestRepository.findAllByIdInAndStatus(
                requestIds, ParticipationRequestStatus.PENDING);
        if (requests.size() != requestIds.size()) {
            throw new ConflictException("Не все заявки находятся в состоянии ожидания");
        }
        return requests;
    }

    private void processConfirmation(EventForRequestDto event, List<ParticipationRequest> requests,
                                     List<ParticipationRequest> confirmed, List<ParticipationRequest> rejected) {
        int currentConfirmed = eventRequestRepository.countByEventIdAndStatus(
                event.getId(), ParticipationRequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit();
        int remaining = limit - currentConfirmed;

        for (ParticipationRequest r : requests) {
            if (remaining > 0) {
                r.setStatus(ParticipationRequestStatus.CONFIRMED);
                confirmed.add(r);
                remaining--;
            } else {
                r.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(r);
            }
        }

        if (remaining == 0) {
            rejectRemainingPending(requests, rejected);
        }
    }

    private void rejectRemainingPending(List<ParticipationRequest> processedRequests,
                                        List<ParticipationRequest> rejectedContainer) {
        List<Long> pendingIds = processedRequests.stream()
                .filter(r -> r.getStatus() == ParticipationRequestStatus.PENDING)
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());
        if (!pendingIds.isEmpty()) {
            List<ParticipationRequest> stillPending = eventRequestRepository.findAllByIdInAndStatus(
                    pendingIds, ParticipationRequestStatus.PENDING);
            for (ParticipationRequest r : stillPending) {
                r.setStatus(ParticipationRequestStatus.REJECTED);
                rejectedContainer.add(r);
            }
        }
    }

    private void rejectAll(List<ParticipationRequest> requests, List<ParticipationRequest> rejected) {
        for (ParticipationRequest r : requests) {
            r.setStatus(ParticipationRequestStatus.REJECTED);
            rejected.add(r);
        }
    }

    private EventRequestStatusUpdateResult buildResult(List<ParticipationRequest> confirmed,
                                                       List<ParticipationRequest> rejected) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed.stream().map(ParticipationRequestMapper::toDto).collect(Collectors.toList()))
                .rejectedRequests(rejected.stream().map(ParticipationRequestMapper::toDto).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public ParticipationRequestDto saveEventParticipation(Long userId, Long eventId) {
        if (eventRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос на добавление пользователя" + userId + "на событие " + eventId + " уже существует");
        }
        UserShortDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
        EventForRequestDto event = eventClient.getEventById(eventId);

        if (event.getInitiatorId().equals(userId))
            throw new ConflictException("Инициатор не может присылать запрос на свое событие");

        if (!"PUBLISHED".equals(event.getState())) {
            throw new ConflictException("Нельзя добавиться в неопубликованное событие");
        }
        ParticipationRequest request = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .created(LocalDateTime.now())
                .build();

        Integer numParticipants = eventRequestRepository.countByEventId(eventId);
        log.info("limit={}, confirmed={}", event.getParticipantLimit(), numParticipants);

        if (event.getParticipantLimit() != 0 && numParticipants >= event.getParticipantLimit()) {
            throw new ConflictException("Количество участников события не может превышать " + event.getParticipantLimit());
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        } else {
            request.setStatus(ParticipationRequestStatus.PENDING);
        }
        return ParticipationRequestMapper.toDto(eventRequestRepository.save(request));
    }

    public List<ParticipationRequestDto> getUserEvents(Long userId) {
        return eventRequestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    public ParticipationRequestDto removeParticipation(Long userId, Long requestId) {
        ParticipationRequest request = eventRequestRepository.findByIdAndRequesterId(requestId, userId);
        eventRequestRepository.delete(request);
        request.setStatus(ParticipationRequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(request);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        return eventRequestRepository.countConfirmedRequestsByEventIds(eventIds).stream()
                .collect(Collectors.toMap(ConfirmedRequestsCount::getEventId, ConfirmedRequestsCount::getCount));
    }
}

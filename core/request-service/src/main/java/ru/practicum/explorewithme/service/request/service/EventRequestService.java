package ru.practicum.explorewithme.service.request.service;

import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;

import java.util.List;
import java.util.Map;

public interface EventRequestService {
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest request);

    ParticipationRequestDto saveEventParticipation(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserEvents(Long userId);

    ParticipationRequestDto removeParticipation(Long userId, Long requestId);

    Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds);
}

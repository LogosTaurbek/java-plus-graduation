package ru.practicum.explorewithme.service.request.mapper;

import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;

public final class ParticipationRequestMapper {

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequesterId())
                .event(request.getEventId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }
}

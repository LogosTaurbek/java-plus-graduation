package ru.practicum.explorewithme.stats.mapper;

import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.model.EndpointHit;

public final class EndpointHitMapper {

    public static EndpointHit toEntity(EndpointHitDTO dto) {
        return new EndpointHit(
                null,
                dto.getApp(),
                dto.getUri(),
                dto.getIp(),
                dto.getTimestamp()
        );
    }
}
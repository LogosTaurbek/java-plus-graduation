package ru.practicum.explorewithme.service.request.client;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.service.exception.NotFoundException;
import ru.practicum.explorewithme.service.request.dto.EventForRequestDto;

@Component("requestEventClientFallback")
public class EventClientFallback implements EventClient {

    @Override
    public EventForRequestDto getEventById(Long eventId) {
        throw new NotFoundException("Сервис событий недоступен (событие " + eventId + ")");
    }
}

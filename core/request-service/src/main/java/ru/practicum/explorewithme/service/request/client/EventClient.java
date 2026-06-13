package ru.practicum.explorewithme.service.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.service.request.dto.EventForRequestDto;

@FeignClient(name = "event-service", contextId = "requestEventClient", fallback = EventClientFallback.class)
public interface EventClient {

    @GetMapping("/internal/events/{eventId}")
    EventForRequestDto getEventById(@PathVariable Long eventId);
}

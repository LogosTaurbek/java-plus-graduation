package ru.practicum.explorewithme.service.compilation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service", contextId = "compilationEventClient")
public interface EventClient {

    @GetMapping("/internal/events")
    List<EventShortDto> getEventsByIds(@RequestParam List<Long> ids);
}

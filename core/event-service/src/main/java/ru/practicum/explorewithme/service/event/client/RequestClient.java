package ru.practicum.explorewithme.service.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", contextId = "eventRequestClient", fallback = RequestClientFallback.class)
public interface RequestClient {

    @GetMapping("/internal/requests/count")
    Map<Long, Long> getConfirmedRequestsCounts(@RequestParam List<Long> eventIds);
}

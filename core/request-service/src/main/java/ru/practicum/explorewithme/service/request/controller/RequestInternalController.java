package ru.practicum.explorewithme.service.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.service.request.service.EventRequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class RequestInternalController {

    private final EventRequestService eventRequestService;

    @GetMapping("/count")
    public Map<Long, Long> getConfirmedRequestsCounts(@RequestParam List<Long> eventIds) {
        return eventRequestService.getConfirmedRequestsCounts(eventIds);
    }
}

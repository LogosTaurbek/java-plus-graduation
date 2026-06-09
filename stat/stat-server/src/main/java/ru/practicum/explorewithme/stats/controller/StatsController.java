package ru.practicum.explorewithme.stats.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;
import ru.practicum.explorewithme.stats.exception.BadRequestException;
import ru.practicum.explorewithme.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody EndpointHitDTO endpointHitDTO) {
        statsService.hit(endpointHitDTO);
    }

    @GetMapping("/stats")
    public List<ViewStatsDTO> getStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false) Boolean unique
    ) {
        if (end.isBefore(start)) {
            throw new BadRequestException("end должен быть позже start");
        }
        return statsService.getStats(start, end, uris, unique);
    }
}

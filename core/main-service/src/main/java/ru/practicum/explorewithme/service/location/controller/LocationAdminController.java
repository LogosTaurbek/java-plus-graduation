package ru.practicum.explorewithme.service.location.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.service.EventService;
import ru.practicum.explorewithme.service.location.dto.LocationDto;
import ru.practicum.explorewithme.service.location.dto.NewLocationRequest;
import ru.practicum.explorewithme.service.location.dto.UpdateLocationRequest;
import ru.practicum.explorewithme.service.location.service.LocationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/locations")
public class LocationAdminController {
    private final LocationService locationService;
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDto saveLocation(@Valid @RequestBody NewLocationRequest request) {
        log.info("Запрос на сохранение новой локации lat - {}, lon - {}, rad - {}",
                request.getLat(), request.getLon(), request.getRadius());
        return locationService.createLocation(request);
    }

    @PatchMapping("/{locId}")
    @ResponseStatus(HttpStatus.OK)
    public LocationDto updateLocation(@PathVariable(name = "locId") Long locId,
                                      @Valid @RequestBody UpdateLocationRequest request) {
        log.info("Запрос на обновление локации lat - {}, lon - {}, rad - {}",
                request.getLat(), request.getLon(), request.getRadius());
        return locationService.updateLocation(locId, request);
    }

    @DeleteMapping("/{locId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLocation(@PathVariable(name = "locId") Long locId) {
        log.info("Запрос на удаление локации id {}", locId);
        locationService.deleteLocation(locId);
    }

    @GetMapping("/{locId}")
    @ResponseStatus(HttpStatus.OK)
    public LocationDto getLocation(@PathVariable(name = "locId") Long locId) {
        log.info("Запрос на получение локации id {}", locId);
        return locationService.getLocationById(locId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<LocationDto> getAllLocations(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return locationService.getAllLocations(from, size);
    }

    @GetMapping("/{locId}/events")
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsByLocation(@PathVariable(name = "locId") Long locId,
                                                  @RequestParam(defaultValue = "0") int from,
                                                  @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос событий в радиусе локации id={}, from={}, size={}", locId, from, size);
        return eventService.getEventsByLocation(locId, from, size);
    }

}

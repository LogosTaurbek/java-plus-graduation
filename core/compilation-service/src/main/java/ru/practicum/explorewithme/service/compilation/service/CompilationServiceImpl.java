package ru.practicum.explorewithme.service.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.compilation.client.EventClient;
import ru.practicum.explorewithme.service.compilation.dal.CompilationRepository;
import ru.practicum.explorewithme.service.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.explorewithme.service.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.service.compilation.model.Compilation;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toEntity(dto);
        List<EventShortDto> events = Collections.emptyList();

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = eventClient.getEventsByIds(dto.getEvents());
            validateEventsExist(events, dto.getEvents());
            compilation.setEventIds(new HashSet<>(dto.getEvents()));
        }

        compilation = compilationRepository.save(compilation);
        log.info("Создана подборка: {}", compilation.getId());
        return CompilationMapper.toDto(compilation, events);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequestDto request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        CompilationMapper.updateEntityFromRequest(request, compilation);
        List<EventShortDto> events;

        if (request.getEvents() != null) {
            events = eventClient.getEventsByIds(request.getEvents());
            validateEventsExist(events, request.getEvents());
            compilation.setEventIds(new HashSet<>(request.getEvents()));
        } else {
            events = fetchEvents(compilation.getEventIds());
        }

        compilation = compilationRepository.save(compilation);
        log.info("Обновлена подборка: {}", compilation.getId());
        return CompilationMapper.toDto(compilation, events);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        compilationRepository.delete(compilation);
        log.info("Удалена подборка: {}", compId);
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        return CompilationMapper.toDto(compilation, fetchEvents(compilation.getEventIds()));
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        List<Compilation> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findByPinned(pinned, pageable).getContent();
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        Set<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventIds().stream())
                .collect(Collectors.toSet());
        Map<Long, EventShortDto> eventsById = fetchEvents(allEventIds).stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return compilations.stream()
                .map(c -> {
                    List<EventShortDto> events = c.getEventIds().stream()
                            .map(eventsById::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return CompilationMapper.toDto(c, events);
                })
                .collect(Collectors.toList());
    }

    private void validateEventsExist(List<EventShortDto> found, List<Long> requestedIds) {
        Set<Long> foundIds = found.stream().map(EventShortDto::getId).collect(Collectors.toSet());
        for (Long id : requestedIds) {
            if (!foundIds.contains(id)) {
                throw new NotFoundException("Событие с id=" + id + " не найдено");
            }
        }
    }

    // Для операций чтения: при недоступности event-service возвращаем пустой список
    private List<EventShortDto> fetchEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyList();
        try {
            return eventClient.getEventsByIds(new ArrayList<>(eventIds));
        } catch (Exception e) {
            log.warn("Не удалось получить события от event-service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}

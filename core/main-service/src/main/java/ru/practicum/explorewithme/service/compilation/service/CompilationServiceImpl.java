package ru.practicum.explorewithme.service.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.service.compilation.dal.CompilationRepository;
import ru.practicum.explorewithme.service.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.service.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.explorewithme.service.compilation.mapper.CompilationMapper;
import ru.practicum.explorewithme.service.compilation.model.Compilation;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private void validateEventsExist(Set<Event> events, List<Long> eventIds) {
        if (eventIds != null && !eventIds.isEmpty()) {
            Set<Long> foundIds = events.stream().map(Event::getId).collect(Collectors.toSet());
            for (Long eventId : eventIds) {
                if (!foundIds.contains(eventId)) {
                    throw new NotFoundException("Событие с id=" + eventId + " не найдено");
                }
            }
        }
    }

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Compilation compilation = CompilationMapper.toEntity(dto);

        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(dto.getEvents()));
            validateEventsExist(events, dto.getEvents());
            compilation.setEvents(events);
        }

        compilation = compilationRepository.save(compilation);
        log.info("Создана подборка: {}", compilation.getId());
        return CompilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequestDto request) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        CompilationMapper.updateEntityFromRequest(request, compilation);

        if (request.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
            validateEventsExist(events, request.getEvents());
            compilation.setEvents(events);
        }

        compilation = compilationRepository.save(compilation);
        log.info("Обновлена подборка: {}", compilation.getId());
        return CompilationMapper.toDto(compilation);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        compilationRepository.delete(compilation);
        log.info("Удалена подборка: {}", compId);
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));
        return CompilationMapper.toDto(compilation);
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

        return compilations.stream().map(CompilationMapper::toDto).collect(Collectors.toList());
    }
}
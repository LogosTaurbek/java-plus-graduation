package ru.practicum.explorewithme.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;
import ru.practicum.explorewithme.stats.mapper.EndpointHitMapper;
import ru.practicum.explorewithme.stats.model.EndpointHit;
import ru.practicum.explorewithme.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void hit(EndpointHitDTO endpointHitDTO) {
        log.info("Сохранение информации о запросе к эндпоинту: app={}, uri={}",
                endpointHitDTO.getApp(), endpointHitDTO.getUri());

        EndpointHit hit = EndpointHitMapper.toEntity(endpointHitDTO);
        statsRepository.save(hit);

        log.debug("Запись успешно сохранена с id={}", hit.getId());
    }

    @Override
    public List<ViewStatsDTO> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        log.info("Запрос статистики: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        // Фильтрация и нормализация списка URI
        List<String> filteredUris = normalizeUris(uris);

        List<ViewStatsDTO> result;
        if (Boolean.TRUE.equals(unique)) {
            result = statsRepository.getStatsUnique(start, end, filteredUris);
        } else {
            result = statsRepository.getStatsNotUnique(start, end, filteredUris);
        }

        log.debug("Найдено записей статистики: {}", result.size());
        return result;
    }

    /**
     * Нормализует список URI:
     * - если список null или пуст — возвращает null (будут выбраны все URI)
     * - удаляет дубликаты и пустые строки
     */
    private List<String> normalizeUris(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return null;
        }
        List<String> cleaned = uris.stream()
                .filter(uri -> uri != null && !uri.isBlank())
                .distinct()
                .collect(Collectors.toList());
        return cleaned.isEmpty() ? null : cleaned;
    }
}
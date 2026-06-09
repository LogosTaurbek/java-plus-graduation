package ru.practicum.explorewithme.stats.service;

import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    /**
     * Сохранить информацию о запросе к эндпоинту.
     *
     * @param endpointHitDTO данные запроса (app, uri, ip, timestamp)
     */
    void hit(EndpointHitDTO endpointHitDTO);

    /**
     * Получить статистику по посещениям за указанный период.
     *
     * @param start  дата и время начала диапазона (включительно)
     * @param end    дата и время конца диапазона (включительно)
     * @param uris   список URI для фильтрации (может быть null или пустым — тогда все URI)
     * @param unique учитывать только уникальные IP (true — да, false/null — нет)
     * @return список объектов статистики, отсортированный по убыванию количества просмотров
     */
    List<ViewStatsDTO> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                Boolean unique);
}
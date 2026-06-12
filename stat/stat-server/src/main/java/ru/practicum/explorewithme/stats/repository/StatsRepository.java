package ru.practicum.explorewithme.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;
import ru.practicum.explorewithme.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    /**
     * Получить статистику без учёта уникальности IP.
     * Группировка по app и uri, подсчёт общего количества записей.
     */
    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStatsDTO(e.app, e.uri, COUNT(e)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR e.uri IN :uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e) DESC")
    List<ViewStatsDTO> getStatsNotUnique(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         @Param("uris") List<String> uris);

    /**
     * Получить статистику с учётом уникальности IP.
     * Группировка по app и uri, подсчёт количества уникальных IP.
     */
    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStatsDTO(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR e.uri IN :uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(DISTINCT e.ip) DESC")
    List<ViewStatsDTO> getStatsUnique(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);
}
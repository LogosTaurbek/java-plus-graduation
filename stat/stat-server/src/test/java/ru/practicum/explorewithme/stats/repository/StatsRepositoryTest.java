package ru.practicum.explorewithme.stats.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;
import ru.practicum.explorewithme.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StatsRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private StatsRepository statsRepository;

    private final LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);

    @BeforeEach
    void setUp() {
        // данные будут добавляться через em
    }

    private void createHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit hit = new EndpointHit(null, app, uri, ip, timestamp);
        em.persist(hit);
    }

    // --- Сохранение ---
    @Test
    void save_ShouldGenerateId() {
        EndpointHit hit = new EndpointHit(null, "app", "/uri", "1.1.1.1", baseTime);
        EndpointHit saved = statsRepository.save(hit);
        assertThat(saved.getId()).isNotNull();
    }

    // --- getStatsNotUnique ---
    @Test
    void getStatsNotUnique_ShouldReturnGroupedCountsOrderedDesc() {
        createHit("ewm-main-service", "/events", "192.168.0.1", baseTime);
        createHit("ewm-main-service", "/events", "192.168.0.2", baseTime.plusMinutes(1));
        createHit("ewm-main-service", "/events/5", "10.0.0.1", baseTime);

        List<ViewStatsDTO> stats = statsRepository.getStatsNotUnique(
                baseTime.minusDays(1), baseTime.plusDays(1), null);

        assertThat(stats).hasSize(2);
        assertThat(stats.get(0).getUri()).isEqualTo("/events");
        assertThat(stats.get(0).getHits()).isEqualTo(2L);
        assertThat(stats.get(1).getUri()).isEqualTo("/events/5");
        assertThat(stats.get(1).getHits()).isEqualTo(1L);
        // проверка сортировки по убыванию hits
        assertThat(stats.get(0).getHits()).isGreaterThan(stats.get(1).getHits());
    }

    @Test
    void getStatsNotUnique_ShouldFilterByUris() {
        createHit("ewm-main-service", "/events", "1.1.1.1", baseTime);
        createHit("ewm-main-service", "/events/1", "2.2.2.2", baseTime);
        createHit("ewm-main-service", "/events/2", "3.3.3.3", baseTime);

        List<ViewStatsDTO> stats = statsRepository.getStatsNotUnique(
                baseTime.minusDays(1), baseTime.plusDays(1), List.of("/events", "/events/1"));

        assertThat(stats).hasSize(2);
        assertThat(stats).extracting(ViewStatsDTO::getUri).containsExactlyInAnyOrder("/events", "/events/1");
    }

    @Test
    void getStatsNotUnique_ShouldRespectDateRange() {
        createHit("app", "/a", "1.1.1.1", baseTime.minusHours(1));
        createHit("app", "/a", "1.1.1.2", baseTime);
        createHit("app", "/a", "1.1.1.3", baseTime.plusHours(1));

        List<ViewStatsDTO> stats = statsRepository.getStatsNotUnique(
                baseTime, baseTime.plusMinutes(30), null);

        // Ожидаем только хит, который попадает в [baseTime, baseTime+30min]
        assertThat(stats).hasSize(1);
        // это тот, что был в baseTime
        assertThat(stats.getFirst().getHits()).isEqualTo(1L);
    }

    @Test
    void getStatsNotUnique_EdgeOfRangeInclusive() {
        createHit("app", "/a", "1.1.1.1", baseTime);
        createHit("app", "/a", "1.1.1.2", baseTime.plusDays(1));

        List<ViewStatsDTO> stats = statsRepository.getStatsNotUnique(
                baseTime, baseTime.plusDays(1), null);
        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getHits()).isEqualTo(2L); // оба входят
    }

    @Test
    void getStatsNotUnique_EmptyResult() {
        List<ViewStatsDTO> stats = statsRepository.getStatsNotUnique(
                baseTime.minusDays(1), baseTime.plusDays(1), null);
        assertThat(stats).isEmpty();
    }

    // --- getStatsUnique ---
    @Test
    void getStatsUnique_ShouldCountDistinctIps() {
        // три хита, но два с одинаковым IP
        createHit("ewm-main-service", "/events", "192.168.0.1", baseTime);
        createHit("ewm-main-service", "/events", "192.168.0.1", baseTime.plusMinutes(1));
        createHit("ewm-main-service", "/events", "10.0.0.1", baseTime);

        List<ViewStatsDTO> stats = statsRepository.getStatsUnique(
                baseTime.minusDays(1), baseTime.plusDays(1), null);

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getHits()).isEqualTo(2L);
    }

    @Test
    void getStatsUnique_ShouldFilterAndSort() {
        createHit("app", "/a", "ip1", baseTime);
        createHit("app", "/a", "ip2", baseTime);
        createHit("app", "/b", "ip1", baseTime);
        createHit("app", "/b", "ip3", baseTime);
        createHit("app", "/b", "ip4", baseTime);

        List<ViewStatsDTO> stats = statsRepository.getStatsUnique(
                baseTime.minusDays(1), baseTime.plusDays(1), null);

        assertThat(stats).hasSize(2);
        assertThat(stats.get(0).getUri()).isEqualTo("/b"); // 3 уникальных ip
        assertThat(stats.get(0).getHits()).isEqualTo(3L);
        assertThat(stats.get(1).getUri()).isEqualTo("/a"); // 2 уникальных ip
        assertThat(stats.get(1).getHits()).isEqualTo(2L);
    }
}
package ru.practicum.explorewithme.stats.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class StatsServiceIntegrationTest {

    @Autowired
    private StatsService statsService;

    private final LocalDateTime start = LocalDateTime.of(2020, 5, 5, 0, 0, 0);
    private final LocalDateTime end = LocalDateTime.of(2035, 5, 5, 0, 0, 0);

    @Test
    void shouldSaveHitAndRetrieveStats() {
        EndpointHitDTO hit = new EndpointHitDTO(null, "ewm-main-service", "/events", "127.0.0.1",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0));
        statsService.hit(hit);

        List<ViewStatsDTO> stats = statsService.getStats(start, end, null, null);
        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getApp()).isEqualTo("ewm-main-service");
        assertThat(stats.getFirst().getUri()).isEqualTo("/events");
        assertThat(stats.getFirst().getHits()).isEqualTo(1L);
    }

    @Test
    void shouldSupportMultipleHitsAndSorting() {
        statsService.hit(new EndpointHitDTO(null, "ewm-main-service", "/events/2", "ip1",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0)));
        statsService.hit(new EndpointHitDTO(null, "ewm-main-service", "/events/1", "ip2",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0)));
        statsService.hit(new EndpointHitDTO(null, "ewm-main-service", "/events/1", "ip3",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0)));

        List<ViewStatsDTO> stats = statsService.getStats(start, end, null, null);

        assertThat(stats).hasSize(2);
        // проверка сортировки по убыванию hits
        assertThat(stats.get(0).getHits()).isGreaterThanOrEqualTo(stats.get(1).getHits());
        assertThat(stats.get(0).getUri()).isEqualTo("/events/1");
        assertThat(stats.get(0).getHits()).isEqualTo(2L);
    }

    @Test
    void shouldHandleUniqueParameter() {
        // три одинаковых посещения с одним IP
        EndpointHitDTO hit = new EndpointHitDTO(null, "ewm-main-service", "/events", "10.0.0.1",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0));
        statsService.hit(hit);
        statsService.hit(hit);
        statsService.hit(hit);

        // без unique
        List<ViewStatsDTO> nonUnique = statsService.getStats(start, end, List.of("/events"), false);
        assertThat(nonUnique.getFirst().getHits()).isEqualTo(3L);

        // с unique = true
        List<ViewStatsDTO> unique = statsService.getStats(start, end, List.of("/events"), true);
        assertThat(unique.getFirst().getHits()).isEqualTo(1L);
    }

    @Test
    void shouldFilterByUris() {
        statsService.hit(new EndpointHitDTO(null, "ewm-main-service", "/events", "1.1.1.1",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0)));
        statsService.hit(new EndpointHitDTO(null, "ewm-main-service", "/events/5", "2.2.2.2",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0)));

        List<ViewStatsDTO> stats = statsService.getStats(start, end, List.of("/events"), null);
        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getUri()).isEqualTo("/events");
    }

    @Test
    void shouldReflectIncrementalHitsExactlyLikePostmanTest() {
        // тест "Корректная работа сохранения и просмотра количества просмотров"
        EndpointHitDTO post1 = new EndpointHitDTO(null, "ewm-main-service", "/events/1", "121.0.0.1",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0));
        EndpointHitDTO post2 = new EndpointHitDTO(null, "ewm-main-service", "/events/2", "121.0.0.1",
                LocalDateTime.of(2025, 5, 1, 10, 0, 0));

        statsService.hit(post1);                 // events/1: 1
        statsService.hit(post2);                 // events/2: 1
        statsService.hit(post2);                 // events/2: 2

        // промежуточный снимок
        List<ViewStatsDTO> source = statsService.getStats(start, end,
                List.of("/events/1", "/events/2"), null);
        // ожидаем: events/2 (2 hits), events/1 (1 hit)
        assertThat(source).hasSize(2);
        ViewStatsDTO source1 = source.stream().filter(s -> s.getUri().equals("/events/1")).findFirst().orElseThrow();
        ViewStatsDTO source2 = source.stream().filter(s -> s.getUri().equals("/events/2")).findFirst().orElseThrow();
        assertThat(source2.getHits()).isGreaterThan(source1.getHits());

        // добавляем ещё по одному
        statsService.hit(post1);                 // events/1: 2
        statsService.hit(post2);                 // events/2: 3

        List<ViewStatsDTO> target = statsService.getStats(start, end,
                List.of("/events/1", "/events/2"), null);

        ViewStatsDTO target1 = target.stream().filter(s -> s.getUri().equals("/events/1")).findFirst().orElseThrow();
        ViewStatsDTO target2 = target.stream().filter(s -> s.getUri().equals("/events/2")).findFirst().orElseThrow();

        // проверка, что хиты увеличились ровно на 1 по сравнению со снимком
        assertThat(target1.getHits()).isEqualTo(source1.getHits() + 1);
        assertThat(target2.getHits()).isEqualTo(source2.getHits() + 1);
        // сортировка: большее количество (events/2) должно быть первым
        assertThat(target.get(0).getHits()).isGreaterThan(target.get(1).getHits());
    }

    @Test
    void shouldReturnEmptyListWhenNoData() {
        List<ViewStatsDTO> stats = statsService.getStats(start, end, null, null);
        assertThat(stats).isEmpty();
    }
}
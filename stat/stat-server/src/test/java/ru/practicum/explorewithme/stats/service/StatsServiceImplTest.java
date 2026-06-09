package ru.practicum.explorewithme.stats.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.stats.dto.EndpointHitDTO;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;
import ru.practicum.explorewithme.stats.model.EndpointHit;
import ru.practicum.explorewithme.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    private StatsRepository statsRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    private EndpointHitDTO hitDTO;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        hitDTO = new EndpointHitDTO(null, "ewm-main-service", "/events", "127.0.0.1",
                LocalDateTime.of(2025, 5, 5, 12, 0, 0));
        start = LocalDateTime.of(2020, 5, 5, 0, 0, 0);
        end = LocalDateTime.of(2030, 5, 5, 0, 0, 0);
    }

    // --- hit ---
    @Test
    void hit_ShouldSaveEntity() {
        statsService.hit(hitDTO);

        ArgumentCaptor<EndpointHit> captor = ArgumentCaptor.forClass(EndpointHit.class);
        verify(statsRepository).save(captor.capture());
        EndpointHit saved = captor.getValue();
        assertThat(saved.getApp()).isEqualTo("ewm-main-service");
        assertThat(saved.getUri()).isEqualTo("/events");
        assertThat(saved.getIp()).isEqualTo("127.0.0.1");
        assertThat(saved.getTimestamp()).isEqualTo(hitDTO.getTimestamp());
    }

    // --- getStats: выбор метода в зависимости от unique ---
    @Test
    void getStats_UniqueTrue_ShouldCallUniqueQuery() {
        List<ViewStatsDTO> expected = List.of(new ViewStatsDTO("app", "/uri", 1L));
        when(statsRepository.getStatsUnique(any(), any(), any())).thenReturn(expected);

        var result = statsService.getStats(start, end, List.of("/uri"), true);

        assertThat(result).isEqualTo(expected);
        verify(statsRepository).getStatsUnique(start, end, List.of("/uri"));
        verify(statsRepository, never()).getStatsNotUnique(any(), any(), any());
    }

    @Test
    void getStats_UniqueFalse_ShouldCallNotUniqueQuery() {
        List<ViewStatsDTO> expected = List.of(new ViewStatsDTO("app", "/uri", 3L));
        when(statsRepository.getStatsNotUnique(any(), any(), any())).thenReturn(expected);

        var result = statsService.getStats(start, end, List.of("/uri"), false);

        assertThat(result).isEqualTo(expected);
        verify(statsRepository).getStatsNotUnique(start, end, List.of("/uri"));
        verify(statsRepository, never()).getStatsUnique(any(), any(), any());
    }

    @Test
    void getStats_UniqueNull_ShouldCallNotUniqueQuery() {
        when(statsRepository.getStatsNotUnique(any(), any(), eq(null))).thenReturn(List.of());

        statsService.getStats(start, end, null, null);

        verify(statsRepository).getStatsNotUnique(start, end, null);
    }

    // --- нормализация uris ---
    @Test
    void getStats_ShouldRemoveBlanksAndDuplicates() {
        when(statsRepository.getStatsNotUnique(any(), any(), eq(List.of("/uri1", "/uri2"))))
                .thenReturn(List.of());

        statsService.getStats(start, end, List.of("", "  ", "/uri1", "/uri2", "/uri1"), false);

        // должен быть передан список без пустых строк и дубликатов
        verify(statsRepository).getStatsNotUnique(start, end, List.of("/uri1", "/uri2"));
    }

    @Test
    void getStats_ShouldTreatEmptyListAsNull() {
        when(statsRepository.getStatsNotUnique(any(), any(), eq(null))).thenReturn(List.of());

        statsService.getStats(start, end, List.of(), false);
        verify(statsRepository).getStatsNotUnique(start, end, null);
    }

    @Test
    void getStats_ShouldTreatAllBlankListAsNull() {
        when(statsRepository.getStatsNotUnique(any(), any(), eq(null))).thenReturn(List.of());

        statsService.getStats(start, end, List.of("", "  "), false);
        verify(statsRepository).getStatsNotUnique(start, end, null);
    }
}
package ru.practicum.explorewithme.service.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParams;
import ru.practicum.explorewithme.service.event.dto.EventShortDto;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.event.model.Location;
import ru.practicum.explorewithme.service.location.dal.LocationRepository;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.dto.ConfirmedRequestsCount;
import ru.practicum.explorewithme.service.user.model.User;
import ru.practicum.explorewithme.stats.client.StatsClient;
import ru.practicum.explorewithme.stats.dto.ViewStatsDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplViewsTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventRequestRepository requestRepository;
    @Mock
    private StatsClient statsClient;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;

    @BeforeEach
    void setUp() {
        Category category = new Category(1L, "Test Category");
        User user = new User(1L, "user@example.com", "User");
        event = new Event();
        event.setId(123L);
        event.setCategory(category);
        event.setInitiator(user);
        event.setAnnotation("Annotation");
        event.setDescription("Description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setLocation(new Location(55f, 37f));
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
        event.setState(EventState.PUBLISHED);
        event.setTitle("Title");
        event.setCreatedOn(LocalDateTime.now().minusDays(1));
    }

    @Test
    void getEventPublic_ShouldReturnViewsAndConfirmedRequests() {
        when(eventRepository.findById(123L)).thenReturn(Optional.of(event));
        when(requestRepository.countByEventIdAndStatus(eq(123L), any())).thenReturn(5);
        
        ViewStatsDTO stat = new ViewStatsDTO("ewm-main-service", "/events/123", 10L);
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(List.of(stat)));

        EventFullDto result = eventService.getEventPublic(123L);

        assertThat(result.getViews()).isEqualTo(10L);
        assertThat(result.getConfirmedRequests()).isEqualTo(5L);
    }

    @Test
    void getEventsPublic_ShouldReturnViewsAndConfirmedRequests() {
        EventSearchParams params = EventSearchParams.builder()
                .from(0)
                .size(10)
                .build();
        
        when(eventRepository.findAll(any(com.querydsl.core.types.Predicate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        
        when(requestRepository.countConfirmedRequestsByEventIds(anyList()))
                .thenReturn(List.of(new ConfirmedRequestsCount(123L, 5L)));
        
        ViewStatsDTO stat = new ViewStatsDTO("ewm-main-service", "/events/123", 10L);
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(List.of(stat)));

        List<EventShortDto> result = eventService.getEventsPublic(params);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getViews()).isEqualTo(10L);
        assertThat(result.get(0).getConfirmedRequests()).isEqualTo(5L);
    }

    @Test
    void getEventsByLocation_ShouldReturnViewsAndConfirmedRequests() {
        ru.practicum.explorewithme.service.location.model.Location loc =
                ru.practicum.explorewithme.service.location.model.Location.builder()
                        .id(1L).lat(55f).lon(37f).radius(10f).build();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(loc));
        when(eventRepository.findEventsByLocation(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(event)));
        
        when(requestRepository.countConfirmedRequestsByEventIds(anyList()))
                .thenReturn(List.of(new ConfirmedRequestsCount(123L, 5L)));
        
        ViewStatsDTO stat = new ViewStatsDTO("ewm-main-service", "/events/123", 10L);
        when(statsClient.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(List.of(stat)));

        List<EventFullDto> result = eventService.getEventsByLocation(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getViews()).isEqualTo(10L);
        assertThat(result.get(0).getConfirmedRequests()).isEqualTo(5L);
    }
}

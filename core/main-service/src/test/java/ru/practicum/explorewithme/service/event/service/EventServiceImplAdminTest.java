package ru.practicum.explorewithme.service.event.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.service.category.dal.CategoryRepository;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.dto.EventFullDto;
import ru.practicum.explorewithme.service.event.dto.EventSearchParamsAdmin;
import ru.practicum.explorewithme.service.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.event.enums.AdminEventStateAction;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.event.model.Location;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplAdminTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRequestRepository requestRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private Category category;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "user@example.com", "User");
        category = new Category(1L, "Category");
        event = new Event();
        event.setId(1L);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setAnnotation("Annotation must be long enough for validation if any");
        event.setDescription("Description must be long enough for validation if any");
        event.setTitle("Title");
        event.setLocation(new Location(55.0f, 37.0f));
        event.setPaid(false);
        event.setParticipantLimit(0);
        event.setRequestModeration(true);
        event.setCreatedOn(LocalDateTime.now());
    }

    @Test
    void getEventsByAdmin_Success() {
        when(eventRepository.findAll(any(BooleanExpression.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(requestRepository.countConfirmedRequestsByEventIds(anyList())).thenReturn(Collections.emptyList());

        EventSearchParamsAdmin params = new EventSearchParamsAdmin(null, null, null, null, null, 0, 10);
        List<EventFullDto> result = eventService.getEventsByAdmin(params);

        assertThat(result).hasSize(1);
        verify(eventRepository).findAll(any(BooleanExpression.class), any(Pageable.class));
    }

    @Test
    void updateEventByAdmin_Publish_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);
        when(requestRepository.countByEventIdAndStatus(anyLong(), any(ParticipationRequestStatus.class))).thenReturn(0);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                .build();

        EventFullDto result = eventService.updateEventByAdmin(1L, request);

        assertThat(result.getState()).isEqualTo(EventState.PUBLISHED);
        assertThat(event.getPublishedOn()).isNotNull();
    }

    @Test
    void updateEventByAdmin_Publish_TooLate_ShouldThrowConflict() {
        event.setEventDate(LocalDateTime.now().plusMinutes(30)); // Less than 1 hour
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                .build();

        assertThatThrownBy(() -> eventService.updateEventByAdmin(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Дата начала события должна быть не ранее чем за час от даты публикации");
    }

    @Test
    void updateEventByAdmin_Publish_NotPending_ShouldThrowConflict() {
        event.setState(EventState.CANCELED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.PUBLISH_EVENT)
                .build();

        assertThatThrownBy(() -> eventService.updateEventByAdmin(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateEventByAdmin_Reject_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenReturn(event);
        when(requestRepository.countByEventIdAndStatus(anyLong(), any(ParticipationRequestStatus.class))).thenReturn(0);

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.REJECT_EVENT)
                .build();

        EventFullDto result = eventService.updateEventByAdmin(1L, request);

        assertThat(result.getState()).isEqualTo(EventState.CANCELED);
    }

    @Test
    void updateEventByAdmin_Reject_AlreadyPublished_ShouldThrowConflict() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .stateAction(AdminEventStateAction.REJECT_EVENT)
                .build();

        assertThatThrownBy(() -> eventService.updateEventByAdmin(1L, request))
                .isInstanceOf(ConflictException.class);
    }
}

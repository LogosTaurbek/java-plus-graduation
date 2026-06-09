package ru.practicum.explorewithme.service.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.service.event.dal.EventRepository;
import ru.practicum.explorewithme.service.event.model.Event;
import ru.practicum.explorewithme.service.exception.ConflictException;
import ru.practicum.explorewithme.service.request.dal.EventRequestRepository;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.service.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.service.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;
import ru.practicum.explorewithme.service.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventRequestServiceImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventRequestRepository eventRequestRepository;

    @InjectMocks
    private EventRequestServiceImpl requestService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setParticipantLimit(2);
        event.setRequestModeration(true);
    }

    @Test
    void getEventRequests_Success() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));
        when(eventRequestRepository.findAllByEventIdAndEventInitiatorId(1L, 1L))
                .thenReturn(List.of());

        List<ParticipationRequestDto> result = requestService.getEventRequests(1L, 1L);
        assertThat(result).isEmpty();
    }

    @Test
    void updateEventRequests_ConfirmWithRemainingLimit() {
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        ParticipationRequest req = new ParticipationRequest();
        req.setId(10L);
        req.setRequester(mockUser);         // <-- установить мок-пользователя
        req.setStatus(ParticipationRequestStatus.PENDING);
        req.setEvent(event);

        when(eventRequestRepository.findAllByIdInAndStatus(List.of(10L), ParticipationRequestStatus.PENDING))
                .thenReturn(List.of(req));
        when(eventRequestRepository.countByEventIdAndStatus(1L, ParticipationRequestStatus.CONFIRMED)).thenReturn(0);
        when(eventRequestRepository.saveAll(anyList())).thenReturn(List.of());

        EventRequestStatusUpdateRequest updateReq = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(10L))
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();

        EventRequestStatusUpdateResult result = requestService.updateEventRequests(1L, 1L, updateReq);

        assertThat(result.getConfirmedRequests()).hasSize(1);
        assertThat(result.getRejectedRequests()).isEmpty();
    }

    @Test
    void updateEventRequests_ConflictWhenPrerequisitesNotMet() {
        event.setParticipantLimit(0);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(event));

        EventRequestStatusUpdateRequest updateReq = EventRequestStatusUpdateRequest.builder()
                .requestIds(List.of(1L))
                .status(ParticipationRequestStatus.CONFIRMED)
                .build();
        assertThatThrownBy(() -> requestService.updateEventRequests(1L, 1L, updateReq))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Подтверждение заявок не требуется");
    }
}

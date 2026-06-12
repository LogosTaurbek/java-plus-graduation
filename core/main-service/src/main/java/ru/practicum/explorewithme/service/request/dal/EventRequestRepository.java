package ru.practicum.explorewithme.service.request.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.service.request.dto.ConfirmedRequestsCount;
import ru.practicum.explorewithme.service.request.enums.ParticipationRequestStatus;
import ru.practicum.explorewithme.service.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface EventRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByEventIdAndEventInitiatorId(Long eventId, Long initiatorId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    Integer countByEventIdAndStatus(Long eventId, ParticipationRequestStatus status);

    List<ParticipationRequest> findAllByIdInAndStatus(List<Long> ids, ParticipationRequestStatus status);

    @Query("SELECT new ru.practicum.explorewithme.service.request.dto.ConfirmedRequestsCount(r.event.id, COUNT(r.id)) " +
            "FROM ParticipationRequest r " +
            "WHERE r.event.id IN :eventIds AND r.status = 'CONFIRMED' " +
            "GROUP BY r.event.id")
    List<ConfirmedRequestsCount> countConfirmedRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Integer countByEventId(Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    ParticipationRequest findByIdAndRequesterId(Long requestId, Long userId);
}

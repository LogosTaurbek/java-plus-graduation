package ru.practicum.explorewithme.service.event.dal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.service.event.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    int countByCategoryId(Long catId);

    @Query(value = "SELECT e.* FROM events e " +
            "JOIN admin_locations l ON l.id = :locId " +
            "WHERE distance(e.lat, e.lon, l.lat, l.lon) <= l.radius " +
            "AND e.state = 'PUBLISHED'",
            countQuery = "SELECT count(*) FROM events e " +
                    "JOIN admin_locations l ON l.id = :locId " +
                    "WHERE distance(e.lat, e.lon, l.lat, l.lon) <= l.radius " +
                    "AND e.state = 'PUBLISHED'",
            nativeQuery = true)
    Page<Event> findEventsByLocation(@Param("locId") Long locId, Pageable pageable);
}

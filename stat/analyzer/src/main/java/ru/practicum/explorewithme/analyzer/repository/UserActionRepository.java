package ru.practicum.explorewithme.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.analyzer.model.UserAction;
import ru.practicum.explorewithme.analyzer.model.UserActionId;

import java.time.Instant;
import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "INSERT INTO user_actions (user_id, event_id, max_weight, timestamp) " +
            "VALUES (:userId, :eventId, :maxWeight, :timestamp) " +
            "ON CONFLICT (user_id, event_id) DO UPDATE SET " +
            "timestamp = EXCLUDED.timestamp, " +
            "max_weight = GREATEST(user_actions.max_weight, EXCLUDED.max_weight)")
    void upsertUserAction(@Param("userId") Long userId,
                          @Param("eventId") Long eventId,
                          @Param("maxWeight") Double maxWeight,
                          @Param("timestamp") Instant timestamp);

    @Query("SELECT e FROM UserAction e WHERE e.id.userId = :userId ORDER BY e.timestamp DESC")
    List<UserAction> findTopNByUser(@Param("userId") Long userId, Pageable pageable);

    @Query(nativeQuery = true, value =
            "SELECT event_id AS eventId, COALESCE(SUM(max_weight), 0.0) AS score " +
            "FROM user_actions WHERE event_id IN :eventIds GROUP BY event_id")
    List<EventScore> findInteractionScores(@Param("eventIds") List<Long> eventIds);
}

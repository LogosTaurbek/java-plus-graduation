package ru.practicum.explorewithme.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.analyzer.model.EventSimilarity;
import ru.practicum.explorewithme.analyzer.model.EventSimilarityId;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value =
            "INSERT INTO event_similarity (event_a_id, event_b_id, score) " +
            "VALUES (:eventAId, :eventBId, :score) " +
            "ON CONFLICT (event_a_id, event_b_id) DO UPDATE SET score = EXCLUDED.score")
    void upsertEventSimilarity(@Param("eventAId") Long eventAId,
                               @Param("eventBId") Long eventBId,
                               @Param("score") Double score);

    @Query("SELECT e FROM EventSimilarity e WHERE e.id.eventAId = :eventId OR e.id.eventBId = :eventId")
    List<EventSimilarity> findByEventId(@Param("eventId") Long eventId);
}

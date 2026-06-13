package ru.practicum.explorewithme.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "event_similarity")
@Getter
@Setter
public class EventSimilarity {

    @EmbeddedId
    private EventSimilarityId id;

    @Column(name = "score", nullable = false)
    private Double score;
}

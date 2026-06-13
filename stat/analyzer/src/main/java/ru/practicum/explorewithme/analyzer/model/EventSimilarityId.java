package ru.practicum.explorewithme.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class EventSimilarityId implements Serializable {

    @Column(name = "event_a_id")
    private Long eventAId;

    @Column(name = "event_b_id")
    private Long eventBId;
}

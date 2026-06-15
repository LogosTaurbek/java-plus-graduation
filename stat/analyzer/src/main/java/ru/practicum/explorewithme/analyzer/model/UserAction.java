package ru.practicum.explorewithme.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_actions")
@Getter
@Setter
public class UserAction {

    @EmbeddedId
    private UserActionId id;

    @Column(name = "max_weight", nullable = false)
    private Double maxWeight;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
}

package ru.practicum.explorewithme.service.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.service.category.model.Category;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 2000)
    String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(nullable = false, length = 7000)
    String description;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;

    @Embedded
    Location location;

    @Column(nullable = false)
    Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    Integer participantLimit;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventState state;

    @Column(nullable = false, length = 120)
    String title;

    @Column(name = "published_on")
    LocalDateTime publishedOn;
}

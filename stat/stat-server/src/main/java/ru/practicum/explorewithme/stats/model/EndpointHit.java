package ru.practicum.explorewithme.stats.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hits")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    String app;

    @Column(nullable = false)
    String uri;

    @Column(nullable = false)
    String ip;

    @Column(name = "time_stamp", nullable = false)
    LocalDateTime timestamp;
}
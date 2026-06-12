package ru.practicum.explorewithme.service.location.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "admin_locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 120, unique = true)
    String name;

    @Column(nullable = false)
    Float lat;

    @Column(nullable = false)
    Float lon;

    @Column(nullable = false)
    Float radius;
}

package ru.practicum.explorewithme.service.compilation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.explorewithme.service.event.model.Event;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "compilations")
@NoArgsConstructor
@Getter
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String title;

    @Setter
    @Column(nullable = false)
    private Boolean pinned;

    @Setter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> events = new HashSet<>();
}
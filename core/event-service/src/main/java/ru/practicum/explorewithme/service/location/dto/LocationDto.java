package ru.practicum.explorewithme.service.location.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    Long id;
    String name;
    Float lat;
    Float lon;
    Float radius;
}

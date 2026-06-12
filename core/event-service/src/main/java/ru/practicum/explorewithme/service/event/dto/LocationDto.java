package ru.practicum.explorewithme.service.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationDto {
    Float lat;
    Float lon;
}

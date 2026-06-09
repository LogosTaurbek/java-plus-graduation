package ru.practicum.explorewithme.service.location.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateLocationRequest {
    @Size(max = 120)
    String name;

    @Min(-90)
    @Max(90)
    Float lat;

    @Min(-180)
    @Max(180)
    Float lon;

    @Positive
    Float radius;
}

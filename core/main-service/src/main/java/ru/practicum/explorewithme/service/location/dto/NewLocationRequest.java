package ru.practicum.explorewithme.service.location.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewLocationRequest {
    @NotBlank
    @Size(max = 120)
    String name;

    @NotNull
    @Min(-90)
    @Max(90)
    Float lat;

    @NotNull
    @Min(-180)
    @Max(180)
    Float lon;

    @NotNull
    @Positive
    Float radius;
}

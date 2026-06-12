package ru.practicum.explorewithme.service.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank(message = "Аннотация не должна быть пустой")
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть от 20 до 2000 символов")
    String annotation;

    @NotNull(message = "Категория должна быть указана")
    Long category;

    @NotBlank(message = "Описание не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Длина описания должна быть от 20 до 7000 символов")
    String description;

    @NotBlank(message = "Дата события должна быть указана")
    String eventDate;

    @NotNull(message = "Местоположение должно быть указано")
    LocationDto location;

    @Builder.Default
    Boolean paid = false;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    @Builder.Default
    Integer participantLimit = 0;

    @Builder.Default
    Boolean requestModeration = true;

    @NotBlank(message = "Заголовок не должен быть пустым")
    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от 3 до 120 символов")
    String title;
}

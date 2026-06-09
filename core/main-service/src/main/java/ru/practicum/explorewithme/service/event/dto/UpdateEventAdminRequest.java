package ru.practicum.explorewithme.service.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.service.event.enums.AdminEventStateAction;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть от 20 до 2000 символов")
    String annotation;

    Long category;

    @Size(min = 20, max = 7000, message = "Длина описания должна быть от 20 до 7000 символов")
    String description;

    String eventDate;

    LocationDto location;

    Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    Boolean requestModeration;

    AdminEventStateAction stateAction;

    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от 3 до 120 символов")
    String title;
}

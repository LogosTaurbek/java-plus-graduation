package ru.practicum.explorewithme.service.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;
    String eventDate;
    UserShortDto initiator;
    Boolean paid;
    String title;
    Long views;
}

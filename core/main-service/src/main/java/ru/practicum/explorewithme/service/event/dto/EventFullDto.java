package ru.practicum.explorewithme.service.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.explorewithme.service.category.dto.CategoryDto;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.user.dto.UserShortDto;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;
    String createdOn;
    String description;
    String eventDate;
    UserShortDto initiator;
    LocationDto location;
    Boolean paid;
    Integer participantLimit;
    String publishedOn;
    Boolean requestModeration;
    EventState state;
    String title;
    Long views;
}

package ru.practicum.explorewithme.service.event.client;

import lombok.Data;

@Data
public class ParticipationRequestDto {
    private Long event;
    private String status;
}

package ru.practicum.explorewithme.service.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmedRequestsCount {
    private Long eventId;
    private Long count;
}

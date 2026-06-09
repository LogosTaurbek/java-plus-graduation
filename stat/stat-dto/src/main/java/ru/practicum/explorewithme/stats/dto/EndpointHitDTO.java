package ru.practicum.explorewithme.stats.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public final class EndpointHitDTO {
    private Long id;

    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    private String app;

    @NotBlank(message = "URI не может быть пустым")
    private String uri;

    @NotBlank(message = "ip не может быть пустым")
    private String ip;

    @NotNull(message = "Дата и время, когда был совершен запрос к эндпоинту, должна быть указана")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}

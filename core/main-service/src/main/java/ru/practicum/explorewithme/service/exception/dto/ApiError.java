package ru.practicum.explorewithme.service.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private String status;
    private String reason;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @Builder.Default
    private List<String> errors = Collections.emptyList();

    // Конструктор для случаев без перечня ошибок (errors = пустой список)
    public ApiError(String status, String reason, String message) {
        this(status, reason, message, Collections.emptyList());
    }

    // Основной конструктор, заполняющий timestamp автоматически
    public ApiError(String status, String reason, String message, List<String> errors) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors != null ? errors : Collections.emptyList();
    }
}

package ru.practicum.explorewithme.service.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCompilationRequestDto {
    @Size(min = 1, max = 50)
    private String title;

    private Boolean pinned;

    private List<Long> events;
}
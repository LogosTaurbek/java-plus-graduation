package ru.practicum.explorewithme.service.category.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCategoryRequest {
    private String name;
}

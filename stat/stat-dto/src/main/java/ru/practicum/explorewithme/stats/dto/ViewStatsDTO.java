package ru.practicum.explorewithme.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsDTO {
    private String app;
    private String uri;
    private Long hits;
}

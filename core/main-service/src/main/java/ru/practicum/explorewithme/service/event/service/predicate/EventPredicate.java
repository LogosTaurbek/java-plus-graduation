package ru.practicum.explorewithme.service.event.service.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import ru.practicum.explorewithme.service.event.dto.EventSearchParams;
import ru.practicum.explorewithme.service.event.dto.EventSearchParamsAdmin;
import ru.practicum.explorewithme.service.event.enums.EventState;
import ru.practicum.explorewithme.service.event.model.QEvent;

import java.time.LocalDateTime;

public class EventPredicate {
    public static BooleanExpression build(EventSearchParams params) {
        QEvent event = QEvent.event;

        BooleanExpression predicate = event.state.eq(EventState.PUBLISHED);

        if (params.getText() != null && !params.getText().isBlank()) {
            predicate = predicate.and(event.annotation.containsIgnoreCase(params.getText()));
        }

        if (params.getCategories() != null && params.getCategories().isEmpty()) {
            predicate = predicate.and(event.category.id.in(params.getCategories()));
        }

        if (params.getPaid() != null) {
            predicate = predicate.and(event.paid.eq(params.getPaid()));

            LocalDateTime start = params.getRangeStart() != null ? params.getRangeStart() : LocalDateTime.now();

            predicate = predicate.and(event.eventDate.goe(start));
        }

        if (params.getRangeEnd() != null) {
            predicate = predicate.and(event.eventDate.loe(params.getRangeEnd()));
        }

        return predicate;
    }

    public static BooleanExpression buildAdmin(EventSearchParamsAdmin params) {
        QEvent event = QEvent.event;
        BooleanExpression predicate = event.isNotNull();

        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            predicate = predicate.and(event.initiator.id.in(params.getUsers()));
        }

        if (params.getStates() != null && !params.getStates().isEmpty()) {
            predicate = predicate.and(event.state.in(params.getStates()));
        }

        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            predicate = predicate.and(event.category.id.in(params.getCategories()));
        }

        if (params.getRangeStart() != null) {
            predicate = predicate.and(event.eventDate.goe(params.getRangeStart()));
        }

        if (params.getRangeEnd() != null) {
            predicate = predicate.and(event.eventDate.loe(params.getRangeEnd()));
        }

        return predicate;
    }
}


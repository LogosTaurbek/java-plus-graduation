package ru.practicum.explorewithme.service.event.enums;

/**
 * Действие пользователя над своим событием:
 * SEND_TO_REVIEW — отправить на модерацию,
 * CANCEL_REVIEW — отменить публикацию (снять с модерации).
 */
public enum UserEventStateAction {
    SEND_TO_REVIEW,
    CANCEL_REVIEW
}

package ru.practicum.explorewithme.stats.exception;


/**
 * @param error       название ошибки
 * @param description подробное описание
 */
@SuppressWarnings("unused")
public record ExceptionResponse(String error, String description) {

}
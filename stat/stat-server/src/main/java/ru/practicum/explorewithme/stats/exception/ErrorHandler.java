package ru.practicum.explorewithme.stats.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@SuppressWarnings("unused")
@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    /*
     Класс для логирования исключений
     и для возвращения более полных текстов ошибок
     и более подходящих кодов ответов.
    */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String errorMessage = (e.getMessage() != null) ? e.getMessage() : "Некорректный формат тела запроса";
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка чтения JSON",
                errorMessage
        );
        log.error("Ошибка десериализации JSON: {}", errorMessage);
        return errorResponse;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: невалидные данные DTO",
                errorMessage
        );
        log.error("Ошибка валидации DTO: {}", errorMessage);
        return errorResponse;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(v -> "Поле: " + v.getPropertyPath() + ". Ошибка: " + v.getMessage())
                .collect(Collectors.joining("; "));

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: невалидный параметр",
                errorMessage
        );
        log.error("Ошибка валидации параметра: {}", errorMessage);
        return errorResponse;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String paramName = e.getName();
        String invalidValue = e.getValue() == null ? "null" : e.getValue().toString();

        String allowedValues = "";
        if (e.getRequiredType() != null && e.getRequiredType().isEnum()) {
            allowedValues = java.util.Arrays.toString(e.getRequiredType().getEnumConstants());
        }

        String errorMessage = String.format(
                "Параметр '%s' со значением '%s' недопустим.%s",
                paramName,
                invalidValue,
                allowedValues.isEmpty() ? "" : " Допустимые значения: " + allowedValues
        );

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: невалидный параметр",
                errorMessage
        );
        log.error("Ошибка валидации параметра: {}", errorMessage);
        return errorResponse;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String errorMessage = e.getAllValidationResults().stream()
                .flatMap(validationResult -> validationResult.getResolvableErrors().stream())
                .map(this::extractErrorMessage)
                .filter(msg -> msg != null && !msg.isEmpty())
                .collect(Collectors.joining("; "));

        if (errorMessage.isEmpty()) {
            errorMessage = "Ошибка валидации входных данных";
        }

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации входных данных",
                errorMessage
        );
        log.error("Ошибка валидации входных данных: {}", errorMessage);
        return errorResponse;
    }

    private String extractErrorMessage(MessageSourceResolvable resolvable) {
        if (resolvable.getDefaultMessage() != null) {
            return resolvable.getDefaultMessage();
        }

        if (resolvable instanceof FieldError fieldError) {
            return fieldError.getDefaultMessage();
        }

        return resolvable.toString();
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleMissingParameters(Exception e) {
        String errorMessage = e.getMessage() != null
                ? e.getMessage()
                : "Отсутствует обязательный параметр";

        ExceptionResponse errorResponse = new ExceptionResponse(
                "Ошибка валидации: отсутствует обязательный параметр",
                errorMessage
        );
        log.error("Отсутствует обязательный параметр: {}", errorMessage);
        return errorResponse;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleBadRequest(BadRequestException e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Некорректный запрос",
                e.getMessage()
        );
        log.error("Некорректный запрос: {}", e.getMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleOtherExceptions(Exception e) {
        ExceptionResponse errorResponse = new ExceptionResponse(
                "Внутренняя ошибка сервера",
                e.getMessage()
        );
        log.error("Произошла непредвиденная ошибка", e);
        return errorResponse;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIntegrityViolation(final DataIntegrityViolationException e) {
        ExceptionResponse response = new ExceptionResponse(
                "Ошибка валидации при сохранении в БД",
                e.getMessage());

        log.warn("Data integrity violation: {}", e.getMessage());

        return response;
    }
}
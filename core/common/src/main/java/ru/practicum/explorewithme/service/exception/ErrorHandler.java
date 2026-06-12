package ru.practicum.explorewithme.service.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explorewithme.service.exception.dto.ApiError;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Slf4j
@RestControllerAdvice(basePackages = "ru.practicum.explorewithme.service")
public class ErrorHandler {

    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDuplicatedDataException(final DuplicatedDataException e) {
        log.warn("Неверные данные: {}", e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST.name(), "Неверные данные", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException e) {
        log.warn("Конфликт данных: {}", e.getMessage());
        return new ApiError(HttpStatus.CONFLICT.name(), "Нарушение целостности данных", e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        return new ApiError(HttpStatus.NOT_FOUND.name(), "Требуемый объект не найден", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream().map(fe -> "Поле: " + fe.getField() + ". Ошибка: " + fe.getDefaultMessage() + ". Значение: " + fe.getRejectedValue()).collect(Collectors.toList());
        log.warn("Ошибка валидации: {}", errors);
        return new ApiError(HttpStatus.BAD_REQUEST.name(), "Некорректный запрос", "Ошибка валидации полей", errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        String message = "Неверный тип параметра '" + e.getName() + "': '" + e.getValue() + "'";
        log.warn("Ошибка конвертации параметра: {}", message);
        return new ApiError(HttpStatus.BAD_REQUEST.name(), "Некорректный запрос", message);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final BadRequestException e) {
        log.warn("Некорректный запрос: {}", e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST.name(), "Некорректный запрос", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDataIntegrityViolation(final DataIntegrityViolationException e) {
        log.warn("Нарушение целостности данных: {}", e.getMostSpecificCause().getMessage());
        return new ApiError(HttpStatus.CONFLICT.name(), "Нарушение целостности данных", e.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.name(), "Внутренняя ошибка сервера", e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(final ConstraintViolationException e) {

        List<String> errors = e.getConstraintViolations().stream().map(v -> "Поле: " + v.getPropertyPath() + ". Ошибка: " + v.getMessage()).collect(Collectors.toList());

        log.warn("Ошибка валидации (Hibernate): {}", errors);

        return new ApiError(HttpStatus.BAD_REQUEST.name(), "Некорректный запрос", "Ошибка валидации Entity", errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingArgumentParameterException(final MissingServletRequestParameterException e) {
        String message = "Отсутствует обязательный параметр" + e.getParameterName();
        log.warn("Ошибка запроса - {}", message);
        return new ApiError(HttpStatus.BAD_REQUEST.name(), "некорректный запрос", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Некорректный JSON: {}", e.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Ошибка в теле запроса",
                "Required request body is missing or invalid"
        );
    }
}
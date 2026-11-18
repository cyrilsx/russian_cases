package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, Locale locale) {
        return build(HttpStatus.NOT_FOUND, messageSource.getMessage("error.notfound", null, locale), locale);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(MethodArgumentNotValidException ex, Locale locale) {
        return build(HttpStatus.BAD_REQUEST, messageSource.getMessage("error.badrequest", null, locale), locale);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, Locale locale) {
        return build(HttpStatus.BAD_REQUEST, messageSource.getMessage("error.badrequest", null, locale), locale);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Locale locale) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message,
                "lang", locale.getLanguage()));
    }
}

package com.cyrilsx.russiancases.web;

import com.cyrilsx.russiancases.service.ResourceNotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex, Locale locale) {
        return build(HttpStatus.CONFLICT, messageSource.getMessage("error.conflict", null, locale), locale);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex, Locale locale) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String key = switch (status) {
            case CONFLICT -> "error.conflict";
            case UNAUTHORIZED -> "error.unauthorized";
            default -> "error.badrequest";
        };
        String message = ex.getReason();
        if (message == null || message.isBlank()) {
            message = messageSource.getMessage(key, null, locale);
        } else {
            try {
                message = messageSource.getMessage(message, null, locale);
            } catch (NoSuchMessageException ignored) {
                // keep the literal reason
            }
        }
        return build(status, message, locale);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Locale locale) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message,
                "lang", locale.getLanguage()));
    }
}

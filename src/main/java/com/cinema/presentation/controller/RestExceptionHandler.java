package com.cinema.presentation.controller;

import com.cinema.domain.Exceptions.AuthorizationException;
import com.cinema.domain.Exceptions.DuplicateException;
import com.cinema.domain.Exceptions.NotFoundException;
import com.cinema.domain.Exceptions.StateTransitionForbidden;
import com.cinema.domain.Exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    // -----------------------
    // Domain exceptions (your custom ones)
    // -----------------------

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("field", ex.field());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("VALIDATION_ERROR", ex.getMessage(), details));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        // NOTE: your class has method recource() (typo) in zip
        details.put("resource", ex.recource());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiError.of("DUPLICATE", ex.getMessage(), details));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("resource", ex.resource());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("NOT_FOUND", ex.getMessage(), details));
    }

    @ExceptionHandler(StateTransitionForbidden.class)
    public ResponseEntity<ApiError> handleStateTransition(StateTransitionForbidden ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiError.of("STATE_TRANSITION_FORBIDDEN", ex.getMessage(), null));
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiError> handleAuthorization(AuthorizationException ex) {
        // Use-case layer sometimes throws "Unauthorized" when actorId==null.
        // Map that to 401. Everything else -> 403.
        String msg = ex.getMessage() == null ? "" : ex.getMessage();
        HttpStatus status = msg.toLowerCase().contains("unauthorized")
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.FORBIDDEN;

        String code = (status == HttpStatus.UNAUTHORIZED) ? "UNAUTHORIZED" : "FORBIDDEN";

        return ResponseEntity
                .status(status)
                .body(ApiError.of(code, ex.getMessage(), null));
    }

    // -----------------------
    // Spring validation errors (DTO validation if you use @Valid)
    // -----------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("errors", ex.getBindingResult().getFieldErrors().stream().map(err -> {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("field", err.getField());
            e.put("message", err.getDefaultMessage());
            return e;
        }).toList());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("VALIDATION_ERROR", "Request validation failed", details));
    }

    // -----------------------
    // Fallback: never leak internals
    // -----------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        // You can log ex here if you want (logger.error)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "An unexpected error occurred", null));
    }

    public record ApiError(String code, String message, Object details, Instant timestamp) {
        public static ApiError of(String code, String message, Object details) {
            return new ApiError(code, message, details, Instant.now());
        }
    }
}

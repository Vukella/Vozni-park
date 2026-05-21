package com.example.vozni_park.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------------
    // Structured error response record
    // ------------------------------------------------------------------

    public record ErrorResponse(
            int status,
            String error,
            String message,
            String errorCode,
            LocalDateTime timestamp
    ) {
        static ErrorResponse of(HttpStatus status, String message, String errorCode) {
            return new ErrorResponse(
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    errorCode,
                    LocalDateTime.now()
            );
        }
    }

    public record ValidationErrorResponse(
            int status,
            String error,
            String message,
            Map<String, String> fieldErrors,
            LocalDateTime timestamp
    ) {}

    // ------------------------------------------------------------------
    // 404 — Entity not found
    // ------------------------------------------------------------------

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), "ENTITY_NOT_FOUND"));
    }

    // ------------------------------------------------------------------
    // 409 — Duplicate entity
    // ------------------------------------------------------------------

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEntity(DuplicateEntityException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), "DUPLICATE_ENTITY"));
    }

    // ------------------------------------------------------------------
    // 422 — Business rule violation (entity exists but operation is invalid)
    // ------------------------------------------------------------------

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(BusinessRuleViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getErrorCode()));
    }

    // ------------------------------------------------------------------
    // 422 — Invalid status transition
    // ------------------------------------------------------------------

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "INVALID_STATUS_TRANSITION"));
    }

    // ------------------------------------------------------------------
    // 403 — Location access denied
    // ------------------------------------------------------------------

    @ExceptionHandler(LocationAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleLocationAccessDenied(LocationAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(HttpStatus.FORBIDDEN, ex.getMessage(), "LOCATION_ACCESS_DENIED"));
    }

    // ------------------------------------------------------------------
    // 400 — Jakarta Validation failures (@Valid on request DTOs)
    // ------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Validation failed. Check field errors for details.",
                        fieldErrors,
                        LocalDateTime.now()
                ));
    }

    // ------------------------------------------------------------------
    // 400 — Illegal argument (fallback for simple input errors)
    // ------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT"));
    }

    // ------------------------------------------------------------------
    // 500 — Unexpected errors (last resort, never swallow details in logs)
    // ------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // Log the full stack trace — the client only sees a generic message
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please contact support.",
                        "INTERNAL_ERROR"
                ));
    }
}

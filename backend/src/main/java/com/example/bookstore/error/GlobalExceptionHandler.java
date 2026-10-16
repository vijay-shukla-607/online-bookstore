package com.example.bookstore.error;

import com.example.bookstore.api.ApiDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> api(ApiException ex, HttpServletRequest req) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("Application error method={} path={} status={}", req.getMethod(), req.getRequestURI(), ex.getStatus().value());
        } else {
            log.debug("Handled API error method={} path={} status={}", req.getMethod(), req.getRequestURI(), ex.getStatus().value());
        }
        return response(ex.getStatus(), ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        log.debug("Request validation failed method={} path={}", req.getMethod(), req.getRequestURI());
        var details = ex.getBindingResult().getFieldErrors().stream().map(e -> new ApiDtos.FieldError(e.getField(), e.getDefaultMessage() == null ? "Invalid value" : e.getDefaultMessage())).toList();
        return response(HttpStatus.BAD_REQUEST, "Request validation failed", req, details);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiDtos.ErrorResponse> badInput(Exception ex, HttpServletRequest req) {
        log.debug("Malformed request rejected method={} path={} exceptionType={}", req.getMethod(), req.getRequestURI(), ex.getClass().getSimpleName());
        return response(HttpStatus.BAD_REQUEST, "Invalid request", req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> conflict(Exception ex, HttpServletRequest req) {
        log.warn("Data conflict method={} path={}", req.getMethod(), req.getRequestURI());
        return response(HttpStatus.CONFLICT, "Request conflicts with existing data", req, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiDtos.ErrorResponse> forbidden(Exception ex, HttpServletRequest req) {
        log.warn("Authorization denied method={} path={}", req.getMethod(), req.getRequestURI());
        return response(HttpStatus.FORBIDDEN, "Access denied", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiDtos.ErrorResponse> unexpected(Exception ex, HttpServletRequest req) {

        log.error("Unexpected error processing {} {}", req.getMethod(), req.getRequestURI(), ex);

        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", req, List.of());
    }

    private ResponseEntity<ApiDtos.ErrorResponse> response(HttpStatus status, String message, HttpServletRequest req, List<ApiDtos.FieldError> details) {
        return ResponseEntity.status(status).body(new ApiDtos.ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI(), details));
    }
}

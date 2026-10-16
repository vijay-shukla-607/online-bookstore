package com.example.bookstore.error;

import com.example.bookstore.api.ApiDtos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;


import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/books/99");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void apiExceptionReturnsItsConfiguredStatus() {
        var exception = ApiException.notFound("Book not found");

        var response = handler.api(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Book not found", response.getBody().message());
        assertEquals("/api/books/99", response.getBody().path());
    }

    @Test
    void conflictReturns409() {
        var exception = new DataIntegrityViolationException("database detail");

        var response = handler.conflict(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
            "Request conflicts with existing data",
            response.getBody().message()
        );
    }

    @Test
    void accessDeniedReturns403() {
        var exception = new AccessDeniedException("internal detail");

        var response = handler.forbidden(exception, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().message());
    }

    @Test
    void malformedJsonReturns400WithoutLeakingDetails() {
        var exception = mock(HttpMessageNotReadableException.class);

        var response = handler.badInput(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request", response.getBody().message());
    }

    @Test
    void constraintViolationReturns400() {
        var exception = mock(ConstraintViolationException.class);

        var response = handler.badInput(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request", response.getBody().message());
    }

    @Test
    void unexpectedExceptionReturnsSafe500Response() {
        var exception =
            new IllegalStateException("sensitive internal database detail");

        var response = handler.unexpected(exception, request);

        assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals(
            "An unexpected error occurred",
            response.getBody().message()
        );
        assertFalse(
            response.getBody().message()
                .contains("sensitive internal database detail")
        );
    }

    @Test
    void errorResponseContainsTimestampAndDetails() {
        var response = handler.api(
            ApiException.badRequest("Invalid page"),
            request
        );

        ApiDtos.ErrorResponse body = response.getBody();

        assertNotNull(body);
        assertNotNull(body.timestamp());
        assertDoesNotThrow(() -> Instant.parse(body.timestamp().toString()));
        assertEquals(400, body.status());
        assertEquals("Bad Request", body.error());
        assertEquals(List.of(), body.details());
    }
}

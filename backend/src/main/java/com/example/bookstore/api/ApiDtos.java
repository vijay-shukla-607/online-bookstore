package com.example.bookstore.api;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ApiDtos {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApiDtos.class);

    private ApiDtos() {
    }

    public record RegisterRequest(@NotBlank @Email @Size(max = 254) String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record AuthResponse(String token, String tokenType, long expiresInSeconds, String email, String role) {
    }

    public record BookRequest(@NotBlank @Size(max = 180) String title,
                              @NotBlank @Size(max = 140) String author,
                              @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal price,
                              @Min(0) @Max(1000000) int stock,
                              @Size(max = 2000) String description) {
    }

    public record BookResponse(Long id, String title, String author, BigDecimal price, int stock, String description) {
    }

    public record AddCartRequest(@NotNull @Positive Long bookId, @Min(1) @Max(1000) int quantity) {
    }

    public record QuantityRequest(@Min(1) @Max(1000) int quantity) {
    }

    public record CartLine(Long bookId, String title, String author, BigDecimal unitPrice, int quantity,
                           BigDecimal lineTotal) {
    }

    public record CartResponse(List<CartLine> items, BigDecimal total) {
    }

    public record OrderLineResponse(Long bookId, String title, int quantity, BigDecimal unitPrice,
                                    BigDecimal lineTotal) {
    }

    public record OrderResponse(Long id, String status, Instant createdAt, BigDecimal total,
                                List<OrderLineResponse> items) {
    }

    public record ErrorResponse(Instant timestamp, int status, String error, String message, String path,
                                List<FieldError> details) {
    }

    public record FieldError(String field, String message) {
    }
}

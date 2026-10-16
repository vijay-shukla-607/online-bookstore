package com.example.bookstore.controller;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.api.OrderController;
import com.example.bookstore.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {
    private OrderService service;
    private OrderController controller;
    private final TestingAuthenticationToken customer = new TestingAuthenticationToken("user@example.com", "", "ROLE_CUSTOMER");
    private final TestingAuthenticationToken admin = new TestingAuthenticationToken("admin@example.com", "", "ROLE_ADMIN");
    private final ApiDtos.OrderResponse order = new ApiDtos.OrderResponse(7L, "PLACED", Instant.parse("2026-01-01T00:00:00Z"), new BigDecimal("25.00"), List.of());

    @BeforeEach
    void setUp() {
        service = mock(OrderService.class);
        controller = new OrderController(service);
    }

    @Test
    void checkoutReturns201() {
        when(service.checkout("user@example.com")).thenReturn(order);
        var response = controller.checkout(customer);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(order, response.getBody());
    }

    @Test
    void checkoutPropagatesEmptyCartFailure() {
        when(service.checkout("user@example.com")).thenThrow(com.example.bookstore.error.ApiException.badRequest("Cart is empty"));
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.checkout(customer));
    }

    @Test
    void listReturnsOnlyCurrentUsersOrders() {
        when(service.list("user@example.com")).thenReturn(List.of(order));
        assertEquals(List.of(order), controller.list(customer));
    }

    @Test
    void getCustomerCallsServiceAsNonAdmin() {
        when(service.get("user@example.com", 7L, false)).thenReturn(order);
        assertEquals(order, controller.get(customer, 7L, customer));
        verify(service).get("user@example.com", 7L, false);
    }

    @Test
    void getAdminCallsServiceWithAdminFlag() {
        when(service.get("admin@example.com", 7L, true)).thenReturn(order);
        assertEquals(order, controller.get(admin, 7L, admin));
        verify(service).get("admin@example.com", 7L, true);
    }

    @Test
    void getPropagatesForbiddenOrMissingOrder() {
        when(service.get("user@example.com", 8L, false)).thenThrow(new com.example.bookstore.error.ApiException(HttpStatus.FORBIDDEN, "Access denied"));
        assertEquals(HttpStatus.FORBIDDEN, assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.get(customer, 8L, customer)).getStatus());
    }
}

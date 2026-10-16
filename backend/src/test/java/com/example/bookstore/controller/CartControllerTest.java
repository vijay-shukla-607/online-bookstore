package com.example.bookstore.controller;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.api.CartController;
import com.example.bookstore.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartControllerTest {
    private CartService service;
    private CartController controller;
    private final TestingAuthenticationToken auth = new TestingAuthenticationToken("user@example.com", "", "ROLE_CUSTOMER");
    private final ApiDtos.CartResponse cart = new ApiDtos.CartResponse(List.of(), BigDecimal.ZERO);

    @BeforeEach
    void setUp() {
        service = mock(CartService.class);
        controller = new CartController(service);
    }

    @Test
    void getReturnsCurrentUsersCart() {
        when(service.get("user@example.com")).thenReturn(cart);
        assertEquals(cart, controller.get(auth));
    }

    @Test
    void addReturns201() {
        var request = new ApiDtos.AddCartRequest(5L, 2);
        when(service.add("user@example.com", request)).thenReturn(cart);
        var response = controller.add(auth, request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(cart, response.getBody());
    }

    @Test
    void addPropagatesMissingBookOrStockFailure() {
        var request = new ApiDtos.AddCartRequest(999L, 2);
        when(service.add("user@example.com", request)).thenThrow(com.example.bookstore.error.ApiException.notFound("Book not found"));
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.add(auth, request));
    }

    @Test
    void quantityUpdatesCart() {
        when(service.setQuantity("user@example.com", 5L, 3)).thenReturn(cart);
        assertEquals(cart, controller.quantity(auth, 5L, new ApiDtos.QuantityRequest(3)));
    }

    @Test
    void quantityPropagatesInvalidCartItemFailure() {
        when(service.setQuantity("user@example.com", 999L, 3)).thenThrow(com.example.bookstore.error.ApiException.notFound("Cart item not found"));
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.quantity(auth, 999L, new ApiDtos.QuantityRequest(3)));
    }

    @Test
    void removeReturns204() {
        var response = controller.remove(auth, 5L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).remove("user@example.com", 5L);
    }
}

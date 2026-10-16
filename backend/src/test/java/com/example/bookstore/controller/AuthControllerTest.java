package com.example.bookstore.controller;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.api.AuthController;
import com.example.bookstore.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    private AuthService service;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        service = mock(AuthService.class);
        controller = new AuthController(service);
    }

    @Test
    void registerReturnsCreatedAndAuthResponse() {
        var request = new ApiDtos.RegisterRequest("user@example.com", "password123");
        var expected = new ApiDtos.AuthResponse("jwt", "Bearer", 3600, "user@example.com", "CUSTOMER");
        when(service.register(request)).thenReturn(expected);
        ResponseEntity<ApiDtos.AuthResponse> response = controller.register(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void registerPropagatesDuplicateAccountFailure() {
        var request = new ApiDtos.RegisterRequest("user@example.com", "password123");
        when(service.register(request)).thenThrow(com.example.bookstore.error.ApiException.conflict("duplicate"));
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.register(request));
    }

    @Test
    void loginReturnsAuthResponse() {
        var request = new ApiDtos.LoginRequest("user@example.com", "password123");
        var expected = new ApiDtos.AuthResponse("jwt", "Bearer", 3600, "user@example.com", "CUSTOMER");
        when(service.login(request)).thenReturn(expected);
        assertEquals(expected, controller.login(request));
    }

    @Test
    void loginPropagatesInvalidCredentialsFailure() {
        var request = new ApiDtos.LoginRequest("user@example.com", "wrong-password");
        when(service.login(request)).thenThrow(new com.example.bookstore.error.ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.login(request)).getStatus());
    }
}

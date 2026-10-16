package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.AppUser;
import com.example.bookstore.domain.Role;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.UserRepository;
import com.example.bookstore.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository users;
    @Mock
    PasswordEncoder encoder;
    @Mock
    AuthenticationManager authManager;
    @Mock
    JwtService jwt;
    @InjectMocks
    AuthService service;

    @Test
    void registerNormalizesEmailEncodesPasswordAndReturnsToken() {
        given(users.existsByEmail("user@example.com")).willReturn(false);
        given(encoder.encode("password123")).willReturn("hashed-password");
        given(users.save(any(AppUser.class))).willAnswer(inv -> inv.getArgument(0));
        given(jwt.generate("user@example.com", "CUSTOMER")).willReturn("jwt-token");
        given(jwt.expirationSeconds()).willReturn(3600L);

        var response = service.register(new ApiDtos.RegisterRequest(" User@Example.com ", "password123"));

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals("user@example.com", response.email());
        assertEquals("CUSTOMER", response.role());
        then(encoder).should().encode("password123");
        then(users).should().save(argThat(u -> u.getEmail().equals("user@example.com") && u.getPasswordHash().equals("hashed-password")));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        given(users.existsByEmail("user@example.com")).willReturn(true);

        ApiException ex = assertThrows(ApiException.class,
            () -> service.register(new ApiDtos.RegisterRequest("User@example.com", "password123")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        then(users).should(never()).save(any(AppUser.class));
        then(encoder).shouldHaveNoInteractions();
    }

    @Test
    void loginAuthenticatesAndReturnsToken() {
        AppUser user = new AppUser("user@example.com", "hash", Role.CUSTOMER);
        given(users.findByEmail("user@example.com")).willReturn(Optional.of(user));
        given(jwt.generate("user@example.com", "CUSTOMER")).willReturn("jwt-token");
        given(jwt.expirationSeconds()).willReturn(3600L);

        var response = service.login(new ApiDtos.LoginRequest("User@Example.com", "password123"));

        assertEquals("jwt-token", response.token());
        then(authManager).should().authenticate(any());
    }

    @Test
    void loginMapsBadCredentialsTo401() {
        willThrow(new BadCredentialsException("bad credentials"))
            .given(authManager).authenticate(any());

        ApiException ex = assertThrows(ApiException.class,
            () -> service.login(new ApiDtos.LoginRequest("user@example.com", "wrong-password")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void loginReturns401IfAuthenticatedUserCannotBeLoaded() {
        given(users.findByEmail("user@example.com")).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
            () -> service.login(new ApiDtos.LoginRequest("user@example.com", "password123")));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}

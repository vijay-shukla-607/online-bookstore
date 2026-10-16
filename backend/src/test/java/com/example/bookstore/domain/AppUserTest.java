package com.example.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AppUserTest {

    @Test
    void shouldCreateUserWithValidValues() {
        AppUser user = new AppUser(
            "customer@example.com",
            "encoded-password",
            Role.CUSTOMER
        );

        assertEquals("customer@example.com", user.getEmail());
        assertEquals("encoded-password", user.getPasswordHash());
        assertEquals(Role.CUSTOMER, user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void shouldCreateAdminUser() {
        AppUser user = new AppUser(
            "admin@example.com",
            "encoded-password",
            Role.ADMIN
        );

        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void shouldInitializeCreatedAtTimestamp() {
        Instant beforeCreation = Instant.now();

        AppUser user = new AppUser(
            "customer@example.com",
            "encoded-password",
            Role.CUSTOMER
        );

        Instant afterCreation = Instant.now();

        assertNotNull(user.getCreatedAt());
        assertFalse(user.getCreatedAt().isBefore(beforeCreation));
        assertFalse(user.getCreatedAt().isAfter(afterCreation));
    }

    @Test
    void shouldRejectNullEmail() {
        assertThrows(NullPointerException.class, () ->
            new AppUser(null, "encoded-password", Role.CUSTOMER)
        );
    }

    @Test
    void shouldRejectBlankEmail() {
        assertThrows(IllegalArgumentException.class, () ->
            new AppUser("   ", "encoded-password", Role.CUSTOMER)
        );
    }

    @Test
    void shouldRejectNullPasswordHash() {
        assertThrows(NullPointerException.class, () ->
            new AppUser("customer@example.com", null, Role.CUSTOMER)
        );
    }

    @Test
    void shouldRejectBlankPasswordHash() {
        assertThrows(IllegalArgumentException.class, () ->
            new AppUser("customer@example.com", "   ", Role.CUSTOMER)
        );
    }

    @Test
    void shouldRejectNullRole() {
        assertThrows(NullPointerException.class, () ->
            new AppUser(
                "customer@example.com",
                "encoded-password",
                null
            )
        );
    }
}

package com.example.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private AppUser createUser() {
        return new AppUser(
            "customer@example.com",
            "encoded-password",
            Role.CUSTOMER
        );
    }

    private Book createBook() {
        return new Book(
            "Clean Code",
            "Robert C. Martin",
            new BigDecimal("499.00"),
            10,
            "Software development book"
        );
    }

    @Test
    void shouldCreateCartItemWithValidQuantity() {
        CartItem item = new CartItem(createUser(), createBook(), 2);

        assertEquals(2, item.getQuantity());
        assertNotNull(item.getUser());
        assertNotNull(item.getBook());
    }

    @Test
    void shouldAcceptMinimumQuantity() {
        CartItem item = new CartItem(createUser(), createBook(), 1);

        assertEquals(1, item.getQuantity());
    }

    @Test
    void shouldAcceptMaximumQuantity() {
        CartItem item = new CartItem(createUser(), createBook(), 1000);

        assertEquals(1000, item.getQuantity());
    }

    @Test
    void shouldRejectZeroQuantityInConstructor() {
        assertThrows(IllegalArgumentException.class,
            () -> new CartItem(createUser(), createBook(), 0));
    }

    @Test
    void shouldRejectNegativeQuantityInConstructor() {
        assertThrows(IllegalArgumentException.class,
            () -> new CartItem(createUser(), createBook(), -1));
    }

    @Test
    void shouldRejectQuantityAboveMaximumInConstructor() {
        assertThrows(IllegalArgumentException.class,
            () -> new CartItem(createUser(), createBook(), 1001));
    }

    @Test
    void shouldUpdateQuantityWhenValid() {
        CartItem item = new CartItem(createUser(), createBook(), 2);

        item.setQuantity(5);

        assertEquals(5, item.getQuantity());
    }

    @Test
    void shouldRejectInvalidQuantityInSetter() {
        CartItem item = new CartItem(createUser(), createBook(), 2);

        assertThrows(IllegalArgumentException.class,
            () -> item.setQuantity(0));

        assertEquals(2, item.getQuantity());
    }

    @Test
    void shouldRejectQuantityAboveMaximumInSetter() {
        CartItem item = new CartItem(createUser(), createBook(), 2);

        assertThrows(IllegalArgumentException.class,
            () -> item.setQuantity(1001));

        assertEquals(2, item.getQuantity());
    }
}

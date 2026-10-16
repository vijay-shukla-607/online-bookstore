package com.example.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void shouldCreateBookWithValidValues() {
        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            new BigDecimal("499.00"),
            10,
            "Software development book"
        );

        assertEquals("Clean Code", book.getTitle());
        assertEquals(new BigDecimal("499.00"), book.getPrice());
        assertEquals(10, book.getStock());
    }

    @Test
    void shouldRejectNullPrice() {
        assertThrows(NullPointerException.class, () ->
            new Book("Clean Code", "Robert C. Martin", null, 10, null)
        );
    }

    @Test
    void shouldRejectZeroPrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new Book("Clean Code", "Robert C. Martin", BigDecimal.ZERO, 10, null)
        );
    }

    @Test
    void shouldRejectNegativeStock() {
        assertThrows(IllegalArgumentException.class, () ->
            new Book("Clean Code", "Robert C. Martin",
                new BigDecimal("100.00"), -1, null)
        );
    }

    @Test
    void shouldRejectInsufficientStock() {
        Book book = new Book(
            "Clean Code", "Robert C. Martin",
            new BigDecimal("100.00"), 2, null
        );

        assertThrows(IllegalStateException.class, () -> book.reduceStock(3));
        assertEquals(2, book.getStock());
    }

    @Test
    void shouldReduceStockWhenQuantityIsValid() {
        Book book = new Book(
            "Clean Code", "Robert C. Martin",
            new BigDecimal("100.00"), 5, null
        );

        book.reduceStock(2);

        assertEquals(3, book.getStock());
    }
}

package com.example.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineTest {

    @Test
    void shouldCreateOrderLineWithValidData() {
        OrderLine line = new OrderLine(
            1L, "Clean Code", 2, new BigDecimal("499.00"));

        assertEquals(1L, line.getBookId());
        assertEquals("Clean Code", line.getTitle());
        assertEquals(2, line.getQuantity());
        assertEquals(0,
            line.getUnitPrice().compareTo(new BigDecimal("499.00")));
    }

    @Test
    void shouldRejectNullBookId() {
        assertThrows(NullPointerException.class,
            () -> new OrderLine(
                null, "Clean Code", 1, new BigDecimal("499.00")));
    }

    @Test
    void shouldRejectInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderLine(
                1L, "Clean Code", 0, new BigDecimal("499.00")));
    }

    @Test
    void shouldRejectBlankTitle() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderLine(
                1L, " ", 1, new BigDecimal("499.00")));
    }

    @Test
    void shouldRejectZeroOrNegativePrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderLine(
                1L, "Clean Code", 1, BigDecimal.ZERO));
    }
}

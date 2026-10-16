package com.example.bookstore.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CustomerOrderTest {

    private AppUser createUser() {
        return new AppUser(
            "customer@example.com",
            "encoded-password",
            Role.CUSTOMER
        );
    }

    @Test
    void shouldCreateOrderWithValidValues() {
        AppUser user = createUser();
        BigDecimal total = new BigDecimal("499.00");

        CustomerOrder order = new CustomerOrder(user, total);

        assertSame(user, order.getUser());
        assertEquals(total, order.getTotal());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().isEmpty());
    }

    @Test
    void shouldAllowZeroTotal() {
        CustomerOrder order = new CustomerOrder(
            createUser(),
            BigDecimal.ZERO
        );

        assertEquals(BigDecimal.ZERO, order.getTotal());
    }

    @Test
    void shouldRejectNullUser() {
        assertThrows(NullPointerException.class, () ->
            new CustomerOrder(null, new BigDecimal("100.00"))
        );
    }

    @Test
    void shouldRejectNullTotal() {
        assertThrows(NullPointerException.class, () ->
            new CustomerOrder(createUser(), null)
        );
    }

    @Test
    void shouldRejectNegativeTotal() {
        assertThrows(IllegalArgumentException.class, () ->
            new CustomerOrder(
                createUser(),
                new BigDecimal("-1.00")
            )
        );
    }

    @Test
    void shouldAddOrderLineAndSetItsOrderReference() {
        CustomerOrder order = new CustomerOrder(
            createUser(),
            new BigDecimal("200.00")
        );

        OrderLine line = new OrderLine(
            10L,
            "Clean Code",
            2,
            new BigDecimal("100.00")
        );

        order.addItem(line);

        assertEquals(1, order.getItems().size());
        assertSame(line, order.getItems().get(0));
    }

    @Test
    void shouldRejectNullOrderLine() {
        CustomerOrder order = new CustomerOrder(
            createUser(),
            new BigDecimal("100.00")
        );

        assertThrows(NullPointerException.class, () ->
            order.addItem(null)
        );

        assertTrue(order.getItems().isEmpty());
    }
}

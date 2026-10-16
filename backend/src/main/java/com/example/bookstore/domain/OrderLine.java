package com.example.bookstore.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@Table(name = "order_lines")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private CustomerOrder order;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected OrderLine() {
    }

    public OrderLine(
        Long bookId,
        String title,
        int quantity,
        BigDecimal unitPrice) {

        this.bookId = Objects.requireNonNull(
            bookId, "Book ID must not be null");

        if (bookId <= 0) {
            throw new IllegalArgumentException(
                "Book ID must be greater than zero");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                "Title must not be blank");
        }

        if (title.length() > 180) {
            throw new IllegalArgumentException(
                "Title must not exceed 180 characters");
        }

        if (quantity < 1 || quantity > 1000) {
            throw new IllegalArgumentException(
                "Quantity must be between 1 and 1000");
        }

        Objects.requireNonNull(
            unitPrice, "Unit price must not be null");

        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Unit price must be greater than zero");
        }

        this.title = title;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void setOrder(CustomerOrder order) {
        this.order = Objects.requireNonNull(
            order, "Order must not be null");
    }
}

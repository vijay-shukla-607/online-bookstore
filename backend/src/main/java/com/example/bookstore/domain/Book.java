package com.example.bookstore.domain;

import jakarta.persistence.*;
import java.util.Objects;
import java.math.BigDecimal;

@Entity
@Table(name = "books", indexes = @Index(name = "idx_book_title", columnList = "title"))
public class Book {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Book.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 180)
    private String title;
    @Column(nullable = false, length = 140)
    private String author;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private int stock;
    @Column(length = 2000)
    private String description;

    protected Book() {
    }

    public Book(String title, String author, BigDecimal price, int stock, String description) {
        validate(price, stock);
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }

    public void update(String title, String author, BigDecimal price, int stock, String description) {
        validate(price, stock);
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    public void reduceStock(int quantity) {
        if (quantity < 1 || stock < quantity) throw new IllegalStateException("Insufficient stock");
        stock -= quantity;
    }

    private void validate(BigDecimal price, int stock) {
        Objects.requireNonNull(price, "Price must not be null");

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}

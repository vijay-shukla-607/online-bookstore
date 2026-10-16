package com.example.bookstore.domain;

import jakarta.persistence.*;
import java.util.Objects;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders", indexes = @Index(name = "idx_order_user_created", columnList = "user_id,created_at"))
public class CustomerOrder {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomerOrder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(AppUser user, BigDecimal total) {
        this.user = Objects.requireNonNull(user, "Order user must not be null");
        this.total = Objects.requireNonNull(total, "Order total must not be null");

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Order total cannot be negative");
        }
    }

    public void addItem(OrderLine line) {
        Objects.requireNonNull(line, "Order line must not be null");
        items.add(line);
        line.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<OrderLine> getItems() {
        return items;
    }
}

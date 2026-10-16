package com.example.bookstore.domain;

import jakarta.persistence.*;
import java.util.Objects;
import java.time.Instant;


@Entity
@Table(name = "app_users", indexes = @Index(name = "idx_user_email", columnList = "email", unique = true))
public class AppUser {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppUser.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.CUSTOMER;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AppUser() {
    }

    public AppUser(String email, String passwordHash, Role role) {
        this.email = Objects.requireNonNull(email, "Email must not be null");
        this.passwordHash = Objects.requireNonNull(
            passwordHash, "Password hash must not be null");
        this.role = Objects.requireNonNull(role, "Role must not be null");

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

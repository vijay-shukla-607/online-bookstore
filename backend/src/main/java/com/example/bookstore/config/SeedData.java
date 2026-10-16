package com.example.bookstore.config;

import com.example.bookstore.domain.AppUser;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.Role;
import com.example.bookstore.repo.BookRepository;
import com.example.bookstore.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Slf4j
@Configuration
public class SeedData {

    @Bean
    CommandLineRunner seed(BookRepository books, UserRepository users, PasswordEncoder encoder,
                           @Value("${app.seed-admin.enabled:true}") boolean seedAdmin,
                           @Value("${app.seed-admin.email:admin@bookstore.local}") String adminEmail,
                           @Value("${app.seed-admin.password:ChangeMe123!}") String adminPassword) {
        return args -> {
            log.info("Bookstore seed-data initialization started");
            if (books.count() == 0) {
                books.save(new Book("Clean Code", "Robert C. Martin", new BigDecimal("42.50"), 25, "A handbook of agile software craftsmanship."));
                books.save(new Book("Effective Java", "Joshua Bloch", new BigDecimal("48.00"), 20, "Best practices for the Java platform."));
                books.save(new Book("Designing Data-Intensive Applications", "Martin Kleppmann", new BigDecimal("55.00"), 12, "Reliable, scalable and maintainable systems."));
            }
            if (seedAdmin && !users.existsByEmail(adminEmail.toLowerCase())) {
                users.save(new AppUser(adminEmail.toLowerCase(), encoder.encode(adminPassword), Role.ADMIN));
            }
            log.info("Bookstore seed-data initialization completed");
        };
    }
}

package com.example.bookstore.api;

import com.example.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;


    @PostMapping("/checkout")
    public ResponseEntity<ApiDtos.OrderResponse> checkout(Authentication auth) {
        log.info("HTTP checkout requested");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(auth.getName()));
    }

    @GetMapping
    public List<ApiDtos.OrderResponse> list(Authentication auth) {
        log.debug("HTTP order history requested");
        return service.list(auth.getName());
    }

    @GetMapping("/{id}")
    public ApiDtos.OrderResponse get(Authentication auth, @PathVariable Long id, Authentication ignored) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        log.debug("HTTP order details requested orderId={} admin={}", id, admin);
        return service.get(auth.getName(), id, admin);
    }
}

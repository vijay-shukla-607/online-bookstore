package com.example.bookstore.api;

import com.example.bookstore.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderController.class);

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiDtos.OrderResponse> checkout(Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.checkout(auth.getName()));
    }

    @GetMapping
    public List<ApiDtos.OrderResponse> list(Authentication auth) {
        return service.list(auth.getName());
    }

    @GetMapping("/{id}")
    public ApiDtos.OrderResponse get(Authentication auth, @PathVariable Long id, org.springframework.security.core.Authentication ignored) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.get(auth.getName(), id, admin);
    }
}

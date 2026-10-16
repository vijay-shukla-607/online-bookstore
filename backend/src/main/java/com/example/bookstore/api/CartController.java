package com.example.bookstore.api;

import com.example.bookstore.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CartController.class);

    private final CartService service;

    @GetMapping
    public ApiDtos.CartResponse get(Authentication auth) {
        return service.get(auth.getName());
    }

    @PostMapping("/items")
    public ResponseEntity<ApiDtos.CartResponse> add(Authentication auth, @Valid @RequestBody ApiDtos.AddCartRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(auth.getName(), req));
    }

    @PatchMapping("/items/{bookId}")
    public ApiDtos.CartResponse quantity(Authentication auth, @PathVariable Long bookId, @Valid @RequestBody ApiDtos.QuantityRequest req) {
        return service.setQuantity(auth.getName(), bookId, req.quantity());
    }

    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> remove(Authentication auth, @PathVariable Long bookId) {
        service.remove(auth.getName(), bookId);
        return ResponseEntity.noContent().build();
    }
}

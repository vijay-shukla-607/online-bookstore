package com.example.bookstore.api;

import com.example.bookstore.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;


    @GetMapping
    public ApiDtos.CartResponse get(Authentication auth) {
        log.debug("HTTP cart retrieval requested");
        return service.get(auth.getName());
    }

    @PostMapping("/items")
    public ResponseEntity<ApiDtos.CartResponse> add(Authentication auth, @Valid @RequestBody ApiDtos.AddCartRequest req) {
        log.info("HTTP add-to-cart requested bookId={} quantity={}", req.bookId(), req.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(auth.getName(), req));
    }

    @PatchMapping("/items/{bookId}")
    public ApiDtos.CartResponse quantity(Authentication auth, @PathVariable Long bookId, @Valid @RequestBody ApiDtos.QuantityRequest req) {
        log.info("HTTP cart quantity update requested bookId={} quantity={}", bookId, req.quantity());
        return service.setQuantity(auth.getName(), bookId, req.quantity());
    }

    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<Void> remove(Authentication auth, @PathVariable Long bookId) {
        log.info("HTTP cart item removal requested bookId={}", bookId);
        service.remove(auth.getName(), bookId);
        return ResponseEntity.noContent().build();
    }
}

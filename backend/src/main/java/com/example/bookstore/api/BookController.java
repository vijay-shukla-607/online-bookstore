package com.example.bookstore.api;

import com.example.bookstore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/books")
public class BookController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookController.class);

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<ApiDtos.BookResponse> list(@RequestParam(required = false) String search) {
        return service.list(search);
    }

    @GetMapping("/page")
    public Page<ApiDtos.BookResponse> page(
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "title") String sortBy,
        @RequestParam(defaultValue = "asc") String direction) {
        if (page < 0) throw com.example.bookstore.error.ApiException.badRequest("page must be >= 0");
        if (size < 1 || size > 100)
            throw com.example.bookstore.error.ApiException.badRequest("size must be between 1 and 100");
        java.util.Set<String> allowed = java.util.Set.of("title", "author", "price", "stock", "id");
        if (!allowed.contains(sortBy))
            throw com.example.bookstore.error.ApiException.badRequest("Unsupported sortBy field");
        Sort.Direction dir;
        try {
            dir = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw com.example.bookstore.error.ApiException.badRequest("direction must be asc or desc");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        return service.page(search, pageable);
    }

    @GetMapping("/{id}")
    public ApiDtos.BookResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDtos.BookResponse> create(@Valid @RequestBody ApiDtos.BookRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiDtos.BookResponse update(@PathVariable Long id, @Valid @RequestBody ApiDtos.BookRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

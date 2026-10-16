package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.AppUser;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.CartItem;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import com.example.bookstore.repo.CartItemRepository;
import com.example.bookstore.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CartService.class);

    private final UserRepository users;
    private final BookRepository books;
    private final CartItemRepository items;

    public CartService(UserRepository users, BookRepository books, CartItemRepository items) {
        this.users = users;
        this.books = books;
        this.items = items;
    }

    public static ApiDtos.CartResponse response(List<CartItem> rows) {
        List<ApiDtos.CartLine> lines = rows.stream()
            .map(CartService::toCartLine)
            .toList();

        BigDecimal total = lines.stream()
            .map(ApiDtos.CartLine::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ApiDtos.CartResponse(lines, total);
    }

    private static ApiDtos.CartLine toCartLine(CartItem item) {
        Book book = item.getBook();
        BigDecimal lineTotal = book.getPrice()
            .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new ApiDtos.CartLine(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getPrice(),
            item.getQuantity(),
            lineTotal
        );
    }


    private AppUser user(String email) {
        return users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional(readOnly = true)
    public ApiDtos.CartResponse get(String email) {
        return response(items.findAllByUserId(user(email).getId()));
    }

    @Transactional
    public ApiDtos.CartResponse add(String email, ApiDtos.AddCartRequest req) {
        AppUser u = user(email);
        Book b = books.findById(req.bookId()).orElseThrow(() -> ApiException.notFound("Book not found"));
        if (b.getStock() < req.quantity()) throw ApiException.conflict("Requested quantity exceeds available stock");
        CartItem item = items.findByUserIdAndBookId(u.getId(), b.getId()).orElse(null);
        int next = req.quantity() + (item == null ? 0 : item.getQuantity());
        if (next > b.getStock()) throw ApiException.conflict("Requested quantity exceeds available stock");
        if (item == null) items.save(new CartItem(u, b, req.quantity()));
        else item.setQuantity(next);
        return response(items.findAllByUserId(u.getId()));
    }

    @Transactional
    public ApiDtos.CartResponse setQuantity(String email, Long bookId, int quantity) {
        // Validate quantity at the service layer
        if (quantity < 1 || quantity > 1000) {
            throw ApiException.badRequest(
                "Quantity must be between 1 and 1000"
            );
        }
        AppUser u = user(email);
        CartItem item = items.findByUserIdAndBookId(u.getId(), bookId).orElseThrow(() -> ApiException.notFound("Cart item not found"));
        if (item.getBook().getStock() < quantity)
            throw ApiException.conflict("Requested quantity exceeds available stock");
        item.setQuantity(quantity);
        return response(items.findAllByUserId(u.getId()));
    }

    @Transactional
    public void remove(String email, Long bookId) {
        AppUser u = user(email);
        CartItem item = items.findByUserIdAndBookId(u.getId(), bookId).orElseThrow(() -> ApiException.notFound("Cart item not found"));
        items.delete(item);
    }
}

package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.*;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import com.example.bookstore.repo.CartItemRepository;
import com.example.bookstore.repo.OrderRepository;
import com.example.bookstore.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderService.class);

    private final UserRepository users;
    private final CartItemRepository cart;
    private final BookRepository books;
    private final OrderRepository orders;

    public OrderService(UserRepository users, CartItemRepository cart, BookRepository books, OrderRepository orders) {
        this.users = users;
        this.cart = cart;
        this.books = books;
        this.orders = orders;
    }

    public static ApiDtos.OrderResponse dto(CustomerOrder o) {
        List<ApiDtos.OrderLineResponse> lines = o.getItems().stream().map(i -> new ApiDtos.OrderLineResponse(i.getBookId(), i.getTitle(), i.getQuantity(), i.getUnitPrice(), i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))).toList();
        return new ApiDtos.OrderResponse(o.getId(), o.getStatus().name(), o.getCreatedAt(), o.getTotal(), lines);
    }

    @Transactional
    public ApiDtos.OrderResponse checkout(String email) {
        AppUser user = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
        List<CartItem> cartItems = cart.findAllByUserId(user.getId());
        if (cartItems.isEmpty()) throw ApiException.badRequest("Cart is empty");
        BigDecimal total = BigDecimal.ZERO;
        java.util.List<OrderLine> orderLines = new java.util.ArrayList<>();
        for (CartItem ci : cartItems) {
            Book locked = books.findByIdForUpdate(ci.getBook().getId()).orElseThrow(() -> ApiException.notFound("Book not found"));
            if (locked.getStock() < ci.getQuantity())
                throw ApiException.conflict("Insufficient stock for book: " + locked.getTitle());
            locked.reduceStock(ci.getQuantity());
            BigDecimal lineTotal = locked.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            total = total.add(lineTotal);
            orderLines.add(new OrderLine(locked.getId(), locked.getTitle(), ci.getQuantity(), locked.getPrice()));
        }
        CustomerOrder saved = new CustomerOrder(user, total);
        orderLines.forEach(saved::addItem);
        CustomerOrder result = orders.save(saved);
        cart.deleteByUserId(user.getId());
        return dto(result);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.OrderResponse> list(String email) {
        AppUser user = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
        return orders.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(OrderService::dto).toList();
    }

    @Transactional(readOnly = true)
    public ApiDtos.OrderResponse get(String email, Long id, boolean admin) {
        CustomerOrder o = orders.findById(id).orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!admin && !o.getUser().getEmail().equals(email))
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        return dto(o);
    }
}

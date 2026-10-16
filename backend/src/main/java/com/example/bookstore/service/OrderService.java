package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.*;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import com.example.bookstore.repo.CartItemRepository;
import com.example.bookstore.repo.OrderRepository;
import com.example.bookstore.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository users;
    private final CartItemRepository cart;
    private final BookRepository books;
    private final OrderRepository orders;


    public static ApiDtos.OrderResponse dto(CustomerOrder order) {
        List<ApiDtos.OrderLineResponse> lines = order.getItems().stream().map(OrderService::toOrderLineResponse).toList();

        return new ApiDtos.OrderResponse(order.getId(), order.getStatus().name(), order.getCreatedAt(), order.getTotal(), lines);
    }

    private static ApiDtos.OrderLineResponse toOrderLineResponse(OrderLine item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new ApiDtos.OrderLineResponse(item.getBookId(), item.getTitle(), item.getQuantity(), item.getUnitPrice(), lineTotal);
    }

    @Transactional
    @CacheEvict(cacheNames = {"books", "bookById"}, allEntries = true)
    public ApiDtos.OrderResponse checkout(String email) {
        log.info("Checkout started");
        AppUser user = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
        List<CartItem> cartItems = cart.findAllByUserId(user.getId());
        if (cartItems.isEmpty()) {
            log.warn("Checkout rejected because cart is empty userId={}", user.getId());
            throw ApiException.badRequest("Cart is empty");
        }
        BigDecimal total = BigDecimal.ZERO;
        List<OrderLine> orderLines = new ArrayList<>();
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
        log.info("Checkout completed orderId={} userId={} itemCount={} total={}", result.getId(), user.getId(), orderLines.size(), total);
        return dto(result);
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.OrderResponse> list(String email) {
        log.debug("Retrieving order history");
        AppUser user = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User not found"));
        List<ApiDtos.OrderResponse> results = orders.findAllByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(OrderService::dto).toList();
        log.debug("Order history retrieved userId={} orderCount={}", user.getId(), results.size());
        return results;
    }

    @Transactional(readOnly = true)
    public ApiDtos.OrderResponse get(String email, Long id, boolean admin) {
        log.debug("Retrieving order orderId={} admin={}", id, admin);
        CustomerOrder o = orders.findById(id).orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!admin && !o.getUser().getEmail().equals(email))
            {
            log.warn("Order access denied orderId={}", id);
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }
        log.debug("Order retrieved orderId={} status={}", o.getId(), o.getStatus());
        return dto(o);
    }
}

package com.example.bookstore.service;

import com.example.bookstore.domain.*;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import com.example.bookstore.repo.CartItemRepository;
import com.example.bookstore.repo.OrderRepository;
import com.example.bookstore.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    UserRepository users;
    @Mock
    CartItemRepository cart;
    @Mock
    BookRepository books;
    @Mock
    OrderRepository orders;
    @InjectMocks
    OrderService service;

    @Test
    void checkoutCreatesOrderReducesStockAndClearsCart() {
        AppUser user = user(1L, "buyer@example.com");
        Book book = book(10L, "Book", "15.00", 7);
        CartItem item = new CartItem(user, book, 2);
        given(users.findByEmail("buyer@example.com")).willReturn(Optional.of(user));
        given(cart.findAllByUserId(1L)).willReturn(List.of(item));
        given(books.findByIdForUpdate(10L)).willReturn(Optional.of(book));
        given(orders.save(any(CustomerOrder.class))).willAnswer(inv -> {
            CustomerOrder order = inv.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 20L);
            return order;
        });

        var response = service.checkout("buyer@example.com");

        assertEquals(20L, response.id());
        assertEquals(new BigDecimal("30.00"), response.total());
        assertEquals(1, response.items().size());
        assertEquals(5, book.getStock());
        then(cart).should().deleteByUserId(1L);
    }

    @Test
    void checkoutRejectsEmptyCart() {
        given(users.findByEmail("buyer@example.com")).willReturn(Optional.of(user(1L, "buyer@example.com")));
        given(cart.findAllByUserId(1L)).willReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> service.checkout("buyer@example.com"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        then(orders).shouldHaveNoInteractions();
        then(cart).should(never()).deleteByUserId(anyLong());
    }

    @Test
    void checkoutRejectsInsufficientStockWithoutSavingOrderOrClearingCart() {
        AppUser user = user(1L, "buyer@example.com");
        Book book = book(10L, "Book", "15.00", 1);
        given(users.findByEmail("buyer@example.com")).willReturn(Optional.of(user));
        given(cart.findAllByUserId(1L)).willReturn(List.of(new CartItem(user, book, 2)));
        given(books.findByIdForUpdate(10L)).willReturn(Optional.of(book));

        ApiException ex = assertThrows(ApiException.class, () -> service.checkout("buyer@example.com"));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        then(orders).shouldHaveNoInteractions();
        then(cart).should(never()).deleteByUserId(anyLong());
    }

    @Test
    void checkoutReturns404WhenUserDoesNotExist() {
        given(users.findByEmail("missing@example.com")).willReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
            assertThrows(ApiException.class, () -> service.checkout("missing@example.com")).getStatus());
        then(cart).shouldHaveNoInteractions();
    }

    @Test
    void listReturnsOnlyRepositoryResultsForCurrentUser() {
        AppUser user = user(1L, "buyer@example.com");
        given(users.findByEmail("buyer@example.com")).willReturn(Optional.of(user));
        given(orders.findAllByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(new CustomerOrder(user, new BigDecimal("5.00"))));

        assertEquals(1, service.list("buyer@example.com").size());
    }

    @Test
    void getAllowsOwnerToReadOrder() {
        AppUser user = user(1L, "buyer@example.com");
        CustomerOrder order = new CustomerOrder(user, new BigDecimal("5.00"));
        ReflectionTestUtils.setField(order, "id", 4L);
        given(orders.findById(4L)).willReturn(Optional.of(order));

        assertEquals(4L, service.get("buyer@example.com", 4L, false).id());
    }

    @Test
    void getRejectsAnotherUsersOrder() {
        AppUser owner = user(1L, "owner@example.com");
        given(orders.findById(4L)).willReturn(Optional.of(new CustomerOrder(owner, new BigDecimal("5.00"))));

        ApiException ex = assertThrows(ApiException.class, () -> service.get("other@example.com", 4L, false));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void adminCanReadAnotherUsersOrder() {
        AppUser owner = user(1L, "owner@example.com");
        CustomerOrder order = new CustomerOrder(owner, new BigDecimal("5.00"));
        ReflectionTestUtils.setField(order, "id", 4L);
        given(orders.findById(4L)).willReturn(Optional.of(order));

        assertEquals(4L, service.get("admin@example.com", 4L, true).id());
    }

    @Test
    void getReturns404ForUnknownOrder() {
        given(orders.findById(404L)).willReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
            assertThrows(ApiException.class, () -> service.get("buyer@example.com", 404L, false)).getStatus());
    }

    private static AppUser user(Long id, String email) {
        AppUser user = new AppUser(email, "hash", Role.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private static Book book(Long id, String title, String price, int stock) {
        Book book = new Book(title, "Author", new BigDecimal(price), stock, "");
        ReflectionTestUtils.setField(book, "id", id);
        return book;
    }
}

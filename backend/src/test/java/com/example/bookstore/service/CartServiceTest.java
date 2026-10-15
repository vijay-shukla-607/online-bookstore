package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.AppUser;
import com.example.bookstore.domain.Book;
import com.example.bookstore.domain.CartItem;
import com.example.bookstore.domain.Role;
import com.example.bookstore.repo.BookRepository;
import com.example.bookstore.repo.CartItemRepository;
import com.example.bookstore.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    UserRepository users;

    @Mock
    BookRepository books;

    @Mock
    CartItemRepository items;

    @InjectMocks
    CartService service;

    @Test
    void addsBookAndCalculatesServerSideTotal() {
        // Arrange
        AppUser user =
            new AppUser("person@example.com", "hash", Role.CUSTOMER);

        Book book = new Book(
            "Test Book",
            "Author",
            new BigDecimal("12.50"),
            10,
            ""
        );

        // Simulate IDs normally assigned by the database.
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(book, "id", 1L);

        given(users.findByEmail("person@example.com"))
            .willReturn(Optional.of(user));

        given(books.findById(1L))
            .willReturn(Optional.of(book));

        given(items.findByUserIdAndBookId(1L, 1L))
            .willReturn(Optional.empty());

        CartItem cartItem = new CartItem(user, book, 2);

        given(items.findAllByUserId(1L))
            .willReturn(List.of(cartItem));

        // Act
        ApiDtos.CartResponse response = service.add(
            "person@example.com",
            new ApiDtos.AddCartRequest(1L, 2)
        );

        // Assert
        assertEquals(
            new BigDecimal("25.00"),
            response.total()
        );

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().get(0).quantity());

        then(items).should().save(any(CartItem.class));
    }
}

package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.Book;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    BookRepository books;
    @InjectMocks
    BookService service;

    @Test
    void listReturnsAllBooksWhenSearchIsNull() {
        Book book = book(1L, "Clean Code", "Robert Martin", "42.50", 5);
        given(books.findAll()).willReturn(List.of(book));

        List<ApiDtos.BookResponse> result = service.list(null);

        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).title());
        then(books).should().findAll();
    }

    @Test
    void listTrimsSearchAndSearchesTitleAndAuthor() {
        given(books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("clean", "clean"))
            .willReturn(List.of(book(1L, "Clean Code", "Robert Martin", "42.50", 5)));

        assertEquals(1, service.list("  clean  ").size());
    }

    @Test
    void pageReturnsMappedResults() {
        var pageable = PageRequest.of(0, 10, Sort.by("title"));
        given(books.findAll(pageable)).willReturn(new PageImpl<>(List.of(book(1L, "A Book", "Author", "10.00", 2))));

        var result = service.page(" ", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("A Book", result.getContent().get(0).title());
    }

    @Test
    void getReturnsBookWhenItExists() {
        given(books.findById(7L)).willReturn(Optional.of(book(7L, "Book", "Author", "9.99", 3)));

        assertEquals(7L, service.get(7L).id());
    }

    @Test
    void getThrows404WhenBookDoesNotExist() {
        given(books.findById(99L)).willReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> service.get(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("Book not found", ex.getMessage());
    }

    @Test
    void createTrimsTitleAndAuthorAndSavesBook() {
        ApiDtos.BookRequest request = new ApiDtos.BookRequest("  Clean Code ", " Robert Martin ", new BigDecimal("42.50"), 5, "Description");
        given(books.save(any(Book.class))).willAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            return book(3L, saved.getTitle(), saved.getAuthor(), saved.getPrice().toPlainString(), saved.getStock());
        });

        ApiDtos.BookResponse response = service.create(request);

        assertEquals(3L, response.id());
        assertEquals("Clean Code", response.title());
        assertEquals("Robert Martin", response.author());
        then(books).should().save(any(Book.class));
    }

    @Test
    void updateChangesExistingBook() {
        Book existing = book(4L, "Old", "Old Author", "10.00", 1);
        given(books.findById(4L)).willReturn(Optional.of(existing));
        var request = new ApiDtos.BookRequest(" New ", " Author ", new BigDecimal("20.00"), 8, "Updated");

        var response = service.update(4L, request);

        assertEquals("New", response.title());
        assertEquals("Author", response.author());
        assertEquals(new BigDecimal("20.00"), response.price());
        assertEquals(8, response.stock());
        then(books).should(never()).save(any(Book.class));
    }

    @Test
    void updateThrows404WhenBookDoesNotExist() {
        given(books.findById(4L)).willReturn(Optional.empty());
        var request = new ApiDtos.BookRequest("New", "Author", new BigDecimal("20.00"), 8, null);

        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ApiException.class, () -> service.update(4L, request)).getStatus());
    }

    @Test
    void deleteRemovesExistingBook() {
        Book existing = book(4L, "Book", "Author", "10.00", 1);
        given(books.findById(4L)).willReturn(Optional.of(existing));

        service.delete(4L);

        then(books).should().delete(existing);
    }

    @Test
    void deleteThrows404WhenBookDoesNotExist() {
        given(books.findById(4L)).willReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ApiException.class, () -> service.delete(4L)).getStatus());
        then(books).should(never()).delete(any(Book.class));
    }

    private static Book book(Long id, String title, String author, String price, int stock) {
        Book book = new Book(title, author, new BigDecimal(price), stock, "Description");
        org.springframework.test.util.ReflectionTestUtils.setField(book, "id", id);
        return book;
    }
}

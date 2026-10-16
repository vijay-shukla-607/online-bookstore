package com.example.bookstore.controller;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.api.BookController;
import com.example.bookstore.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerUnitTest {
    private BookService service;
    private BookController controller;
    private final ApiDtos.BookResponse book = new ApiDtos.BookResponse(1L, "Clean Code", "Robert Martin", new BigDecimal("42.50"), 4, "Description");

    @BeforeEach
    void setUp() {
        service = mock(BookService.class);
        controller = new BookController(service);
    }

    @Test
    void listReturnsBooks() {
        when(service.list("clean")).thenReturn(List.of(book));
        assertEquals(List.of(book), controller.list("clean"));
    }

    @Test
    void getReturnsBook() {
        when(service.get(1L)).thenReturn(book);
        assertEquals(book, controller.get(1L));
    }

    @Test
    void createReturns201() {
        var request = new ApiDtos.BookRequest("Clean Code", "Robert Martin", new BigDecimal("42.50"), 4, "Description");
        when(service.create(request)).thenReturn(book);
        var response = controller.create(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(book, response.getBody());
    }

    @Test
    void updateReturnsUpdatedBook() {
        var request = new ApiDtos.BookRequest("Clean Code", "Robert Martin", new BigDecimal("42.50"), 4, "Description");
        when(service.update(1L, request)).thenReturn(book);
        assertEquals(book, controller.update(1L, request));
    }

    @Test
    void deleteReturns204() {
        doNothing().when(service).delete(1L);
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(1L).getStatusCode());
        verify(service).delete(1L);
    }

    @Test
    void pageRejectsNegativePage() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.page(null, -1, 10, "title", "asc")).getStatus());
    }

    @Test
    void pageRejectsTooLargeSize() {
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.page(null, 0, 101, "title", "asc"));
    }

    @Test
    void pageRejectsUnsupportedSortField() {
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.page(null, 0, 10, "password", "asc"));
    }

    @Test
    void pageRejectsInvalidDirection() {
        assertThrows(com.example.bookstore.error.ApiException.class, () -> controller.page(null, 0, 10, "title", "sideways"));
    }

    @Test
    void pagePassesValidRequestToService() {
        when(service.page(eq("clean"), any())).thenReturn(new PageImpl<>(List.of(book), PageRequest.of(0, 10), 1));
        assertEquals(1, controller.page("clean", 0, 10, "title", "asc").getTotalElements());
        verify(service).page(eq("clean"), any());
    }
}

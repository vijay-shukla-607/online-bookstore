package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.Book;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookService.class);

    private final BookRepository books;

    public static ApiDtos.BookResponse dto(Book b) {
        return new ApiDtos.BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getPrice(), b.getStock(), b.getDescription());
    }

    @Transactional(readOnly = true)
    public List<ApiDtos.BookResponse> list(String search) {
        List<Book> rows = search == null || search.isBlank() ? books.findAll() :
            books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search.trim(), search.trim());
        return rows.stream().map(BookService::dto).toList();
    }

    @Transactional(readOnly = true)
    public Page<ApiDtos.BookResponse> page(String search, Pageable pageable) {
        Page<Book> rows = search == null || search.isBlank() ? books.findAll(pageable) :
            books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search.trim(), search.trim(), pageable);
        return rows.map(BookService::dto);
    }

    @Transactional(readOnly = true)
    public ApiDtos.BookResponse get(Long id) {
        return dto(books.findById(id).orElseThrow(() -> ApiException.notFound("Book not found")));
    }

    @Transactional
    public ApiDtos.BookResponse create(ApiDtos.BookRequest req) {
        return dto(books.save(new Book(req.title().trim(), req.author().trim(), req.price(), req.stock(), req.description())));
    }

    @Transactional
    public ApiDtos.BookResponse update(Long id, ApiDtos.BookRequest req) {
        Book b = books.findById(id).orElseThrow(() -> ApiException.notFound("Book not found"));
        b.update(req.title().trim(), req.author().trim(), req.price(), req.stock(), req.description());
        return dto(b);
    }

    @Transactional
    public void delete(Long id) {
        Book b = books.findById(id).orElseThrow(() -> ApiException.notFound("Book not found"));
        books.delete(b);
    }
}

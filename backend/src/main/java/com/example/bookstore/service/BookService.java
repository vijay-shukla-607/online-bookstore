package com.example.bookstore.service;

import com.example.bookstore.api.ApiDtos;
import com.example.bookstore.domain.Book;
import com.example.bookstore.error.ApiException;
import com.example.bookstore.repo.BookRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookService {

    private final BookRepository books;


    public static ApiDtos.BookResponse dto(Book b) {
        return new ApiDtos.BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getPrice(), b.getStock(), b.getDescription());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "books", key = "'all'", condition = "#search == null || #search.isBlank()")
    public List<ApiDtos.BookResponse> list(String search) {
        log.debug("Loading book catalog searchPresent={}", search != null && !search.isBlank());
        List<Book> rows = search == null || search.isBlank() ? books.findAll() :
            books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search.trim(), search.trim());
        log.debug("Book catalog loaded resultCount={}", rows.size());
        return rows.stream().map(BookService::dto).toList();
    }

    @Transactional(readOnly = true)
    public Page<ApiDtos.BookResponse> page(String search, Pageable pageable) {
        Page<Book> rows = search == null || search.isBlank() ? books.findAll(pageable) :
            books.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search.trim(), search.trim(), pageable);
        log.debug("Paged book catalog loaded page={} size={} resultCount={}", pageable.getPageNumber(), pageable.getPageSize(), rows.getNumberOfElements());
        return rows.map(BookService::dto);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "bookById", key = "#id")
    public ApiDtos.BookResponse get(Long id) {
        log.debug("Loading book details bookId={}", id);
        return dto(books.findById(id).orElseThrow(() -> ApiException.notFound("Book not found")));
    }

    @Transactional
    @CacheEvict(cacheNames = {"books", "bookById"}, allEntries = true)
    public ApiDtos.BookResponse create(ApiDtos.BookRequest req) {
        ApiDtos.BookResponse created = dto(books.save(new Book(req.title().trim(), req.author().trim(), req.price(), req.stock(), req.description())));
        log.info("Book created bookId={}", created.id());
        return created;
    }

    @Transactional
    @CacheEvict(cacheNames = {"books", "bookById"}, allEntries = true)
    public ApiDtos.BookResponse update(Long id, ApiDtos.BookRequest req) {
        log.info("Updating book bookId={}", id);
        Book b = books.findById(id).orElseThrow(() -> ApiException.notFound("Book not found"));
        b.update(req.title().trim(), req.author().trim(), req.price(), req.stock(), req.description());
        log.info("Book updated bookId={}", id);
        return dto(b);
    }

    @Transactional
    @CacheEvict(cacheNames = {"books", "bookById"}, allEntries = true)
    public void delete(Long id) {
        log.info("Deleting book bookId={}", id);
        Book b = books.findById(id).orElseThrow(() -> ApiException.notFound("Book not found"));
        books.delete(b);
        log.info("Book deleted bookId={}", id);
    }
}

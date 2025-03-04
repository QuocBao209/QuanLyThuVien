package com.project.demo.service;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.repository.BookRepository;
import com.project.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public Optional<Book> findByBookNameAndAuthors(String bookName, List<Author> authors) {
        return bookRepository.findByBookNameAndIsDeletedFalse(bookName).stream()
                .filter(book -> new HashSet<>(book.getAuthors()).containsAll(authors))
                .findFirst();
    }

    public Optional<Book> findExactMatch(String bookName, List<Author> authors, Category category, int publishYear) {
        return bookRepository.findByIsDeletedFalse().stream()
                .filter(b -> b.getBookName().equalsIgnoreCase(bookName) &&
                        b.getPublishYear() == publishYear &&
                        Objects.equals(b.getCategory(), category) &&
                        new HashSet<>(b.getAuthors()).equals(new HashSet<>(authors)))
                .findFirst();
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findByIsDeletedFalse();
        }
        return bookRepository.findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(keyword, keyword);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    // Lưu sách mới hoặc cập nhật sách
    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    // Cập nhật thông tin sách (nếu tồn tại)
    public void updateBook(Book book) {
        if (bookRepository.existsById(book.getBookId())) {
            bookRepository.save(book);
        }
    }
    public String getBookImagePath(Long bookId) {
        return bookRepository.findBookImagePathById(bookId);
    }

    // Xóa sách theo ID
    public void deleteBook(Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setDeleted(true);
            bookRepository.save(book);
        });
    }

    public List<Book> getBooks() {
        return bookRepository.findByIsDeletedFalse();
    }

    // Chuyển dữ liệu danh sách sách vào database
    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }

    // Xử lý phân trang
    public Page<Book> getBooksByAuthors(List<Author> authors, Long excludeBookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByAuthors(authors, excludeBookId, pageable);
    }
}

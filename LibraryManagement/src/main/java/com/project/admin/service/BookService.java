package com.project.admin.service;

import com.project.admin.entity.Author;
import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public Optional<Book> findByBookNameAndAuthors(String bookName, List<Author> authors, Category category, int publishYear) {
        return bookRepository.findByIsDeletedFalse().stream()
                .filter(book -> book.getBookName().equalsIgnoreCase(bookName) &&
                        book.getPublishYear() == publishYear &&
                        Objects.equals(book.getCategory(), category) &&
                        new HashSet<>(book.getAuthors()).equals(new HashSet<>(authors))) // So sánh danh sách tác giả
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
        return bookRepository.findById(id).filter(book -> !book.isDeleted()).orElse(null);
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setDeleted(true);
            bookRepository.save(book);
        });
    }

    public List<Book> getBooks() {
        return bookRepository.findByIsDeletedFalse();
    }


    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }
}

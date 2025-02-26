package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import com.project.admin.entity.Author;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    private CategoryRepository categoryRepository;

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> findByBookNameAndAuthor(String bookName, Author author) {
        return bookRepository.findByBookNameAndAuthor(bookName, author);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    public void updateBook(Book book) {
        if (bookRepository.existsById(book.getBookId())) {
            bookRepository.save(book);
        }
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }
}

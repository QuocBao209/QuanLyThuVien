package com.project.demo.service;

import com.project.demo.entity.Book;
import com.project.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired private BookRepository bookRepository;

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }

    public Optional<Book> findByBookTitleAndAuthor(String bookTitle, String authorName) {
        return bookRepository.findByBookTitleAndAuthor(bookTitle, authorName);
    }

    public Book saveBook(Book book) {
        return bookRepository.save(book);
    }
}

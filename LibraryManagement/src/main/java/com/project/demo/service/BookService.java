package com.project.demo.service;

import com.project.demo.entity.Book;
import com.project.demo.entity.Author;
import com.project.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> findByBookNameAndAuthor(String bookName, Author author) {
        return bookRepository.findByBookNameAndAuthor(bookName, author);
    }

    public void saveBook(Book book) {
        bookRepository.save(book);
    }

    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }
}

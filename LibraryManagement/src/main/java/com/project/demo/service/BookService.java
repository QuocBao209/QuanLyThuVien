package com.project.demo.service;

import com.project.demo.entity.Book;
import com.project.demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired private BookRepository bookRepository;

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }
}

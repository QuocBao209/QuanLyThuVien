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


    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return bookRepository.findByIsDeletedFalse();
        }
        return bookRepository.findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(keyword, keyword);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElse(null);
    }



    public List<Book> getBooks() {
        return bookRepository.findByIsDeletedFalse();
    }

    public Page<Book> filterBooks(Set<String> categoryNames, Set<String> timeRanges, String bookName,  Pageable pageable) {
        Integer startYear = null;
        Integer endYear = null;

        if (timeRanges != null && !timeRanges.isEmpty()) {
            OptionalInt minYear = timeRanges.stream()
                    .mapToInt(range -> Integer.parseInt(range.split("-")[0]))
                    .min();
            OptionalInt maxYear = timeRanges.stream()
                    .mapToInt(range -> Integer.parseInt(range.split("-")[1]))
                    .max();

            if (minYear.isPresent() && maxYear.isPresent()) {
                startYear = minYear.getAsInt();
                endYear = maxYear.getAsInt();
            }
        }

        return bookRepository.findFilteredBooks(categoryNames, startYear, endYear,  pageable);
    }

    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }

    public Page<Book> getBooksByAuthors(List<Author> authors, Long excludeBookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByAuthors(authors, excludeBookId, pageable);
    }

}

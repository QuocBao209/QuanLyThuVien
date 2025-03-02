package com.project.demo.service;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.repository.BookRepository;
import com.project.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy danh sách tất cả sách
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    // Tìm sách theo tên và danh sách tác giả
    public Optional<Book> findByBookNameAndAuthors(String bookName, List<Author> authors) {
        return bookRepository.findAll().stream()
                .filter(book -> book.getBookName().equalsIgnoreCase(bookName) &&
                        new HashSet<>(book.getAuthors()).containsAll(authors))
                .findFirst();
    }

    // Lấy sách theo ID
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
        bookRepository.deleteById(id);
    }

    // Chuyển dữ liệu danh sách sách vào database
    public void transferData(List<Book> books) {
        bookRepository.saveAll(books);
    }
}

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
                        new HashSet<>(book.getAuthors()).equals(new HashSet<>(authors)))
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

    public void save(Book book) {
        bookRepository.save(book);
    }

    // Thống kê sách theo dữ liệu được nhập trên vùng tìm kiếm
    public List<Book> getBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        // Trường hợp 1: Chỉ nhập tên sách
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllBooks(query);
        }

        // Trường hợp 11: Chỉ chọn thể loại
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByCategory(query, categoryId);
        }

        // Trường hợp 7: Chỉ nhập fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBooksByYear(query, fromYear);
        }

        // Trường hợp 8: Nhập fromYear và chọn thể loại
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Trường hợp 5: Chỉ nhập fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBooksByMonthAndYear(query, fromMonth, fromYear);
        }

        // Trường hợp 6: Nhập fromMonth - fromYear và chọn thể loại
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Trường hợp 9: Chỉ nhập khoảng thời gian
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Trường hợp 10: Nhập khoảng thời gian và thể loại
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }

        // Trường hợp 2: Nhập tên sách và fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Trường hợp 3: Nhập tên sách và fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Mặc định: Trả về tất cả sách nếu không khớp
        return bookRepository.findAllBooks(query);
    }

    // Thống kê sách đang được mượn
    public List<Book> getBorrowingBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        // Trường hợp 1: Chỉ nhập tên sách
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllBorrowedBooks(query);
        }

        // Trường hợp 11: Chỉ chọn thể loại
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByCategory(query, categoryId);
        }

        // Trường hợp 7: Chỉ nhập fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBorrowedBooksByYear(query, fromYear);
        }

        // Trường hợp 8: Nhập fromYear và chọn thể loại
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Trường hợp 5: Chỉ nhập fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBorrowedBooksByMonthAndYear(query, fromMonth, fromYear);
        }

        // Trường hợp 6: Nhập fromMonth - fromYear và chọn thể loại
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Trường hợp 9: Chỉ nhập khoảng thời gian
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findBorrowedBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Trường hợp 10: Nhập khoảng thời gian và thể loại
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findBorrowedBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }

        // Trường hợp 2: Nhập tên sách và fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBorrowedBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Trường hợp 3: Nhập tên sách và fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBorrowedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Mặc định: Trả về tất cả sách đang mượn
        return bookRepository.findAllBorrowedBooks(query);
    }
}
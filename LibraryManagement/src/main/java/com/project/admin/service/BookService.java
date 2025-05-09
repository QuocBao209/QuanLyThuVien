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

    // All
    // Thống kê sách theo dữ liệu được nhập trên vùng tìm kiếm
    public List<Book> getBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear,
                                             Integer toMonth, Integer toYear, Integer categoryId) {

        // Chỉ nhập tên sách
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllBooks(query);
        }

        // Chỉ chọn thể loại
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByCategory(query, categoryId);
        }

        // Chỉ nhập fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBooksByYear(query, fromYear);
        }

        // Nhập fromYear và chọn thể loại
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Chỉ nhập fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBooksByMonthAndYear(query, fromMonth, fromYear);
        }

        // Nhập fromMonth - fromYear và chọn thể loại
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Chỉ nhập khoảng thời gian
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Nhập khoảng thời gian và thể loại
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }

        // Nhập tên sách và fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Nhập tên sách và fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Trả về tất cả sách nếu không khớp
        return bookRepository.findAllBooks(query);
    }

    // Borrowing
    // Thống kê sách đang được mượn
    public List<Book> getBorrowingBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear,
                                                      Integer toMonth, Integer toYear, Integer categoryId) {

        // Chỉ nhập tên sách
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllBorrowedBooks(query);
        }

        // Chỉ chọn thể loại
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByCategory(query, categoryId);
        }

        // Chỉ nhập fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBorrowedBooksByYear(query, fromYear);
        }

        // Nhập fromYear và chọn thể loại
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Chỉ nhập fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBorrowedBooksByMonthAndYear(query, fromMonth, fromYear);
        }

        // Nhập fromMonth - fromYear và chọn thể loại
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Chỉ nhập khoảng thời gian
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findBorrowedBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Nhập khoảng thời gian và thể loại
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findBorrowedBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }

        // Nhập tên sách và fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBorrowedBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Nhập tên sách và fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBorrowedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Trả về tất cả sách đang mượn
        return bookRepository.findAllBorrowedBooks(query);
    }

    // Ready
    // Thống kê sách đang sẵn sàng
    public List<Book> getReadyBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear,
                                                  Integer toMonth, Integer toYear, Integer categoryId) {
        // Chỉ nhập tên sách
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllReadyBooks(query);
        }

        // Chỉ chọn thể loại
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findReadyBooksByCategory(query, categoryId);
        }

        // Chỉ nhập fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findReadyBooksByYear(query, fromYear);
        }

        // Nhập fromYear và chọn thể loại
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findReadyBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Chỉ nhập fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findReadyBooksByMonthAndYear(query, fromMonth, fromYear);
        }

        // Nhập fromMonth - fromYear và chọn thể loại
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findReadyBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Chỉ nhập khoảng thời gian
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findReadyBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Nhập khoảng thời gian và thể loại
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findReadyBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }

        // Nhập tên sách và fromYear
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findReadyBooksByYearAndCategory(query, fromYear, categoryId);
        }

        // Nhập tên sách và fromMonth - fromYear
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findReadyBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }

        // Trả về tất cả sách đang mượn
        return bookRepository.findAllReadyBooks(query);
    }
}
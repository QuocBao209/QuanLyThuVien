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

    public void save(Book book) {
        bookRepository.save(book);
    }
    
    // Thống kê sách theo dữ liệu được nhập trên vùng tìm kiếm
    public List<Book> getBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        // Nếu tất cả tháng, năm đều null
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null) {
            if (categoryId == null) {
                return bookRepository.findAllBooks(query); // Trả về tất cả sách
            } else {
                return bookRepository.findBooksByCategory(query, categoryId); // Trả về sách theo category
            }
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null
        	|| fromMonth == null && fromYear == null && toMonth != null && toYear != null) {
        	return bookRepository.findBooksByMonthAndYear(query, fromMonth, fromYear, categoryId, categoryId);
        }
        	

        // Nếu tháng, năm được cung cấp nhưng không có categoryId
        if (categoryId == null) {
            return bookRepository.findBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Nếu tất cả đều được cung cấp (có cả categoryId và khoảng thời gian)
        return bookRepository.findBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
    }
    
    // Thống kê sách đang được mượn
    public List<Book> getBorrowingBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        // Nếu tất cả tháng, năm đều null
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null) {
            if (categoryId == null) {
                return bookRepository.findAllBorrowedBooks(query); // Trả về tất cả sách
            } else {
                return bookRepository.findBorrowedBooksByCategory(query, categoryId); // Trả về sách theo category
            }
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null
        	|| fromMonth == null && fromYear == null && toMonth != null && toYear != null) {
        	return bookRepository.findBorrowedBooksByMonthAndYear(query, fromMonth, fromYear, categoryId, categoryId);
        }
        	

        // Nếu tháng, năm được cung cấp nhưng không có categoryId
        if (categoryId == null) {
            return bookRepository.findBorrowedBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Nếu tất cả đều được cung cấp (có cả categoryId và khoảng thời gian)
        return bookRepository.findBorrowedBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
    }

 // Thống kê sách đang sẵn sàng
    public List<Book> getReadyBooksByMonthAndYear(String query, Integer categoryId) {


            if (categoryId == null) {
                return bookRepository.findAllReadyBooks(query); // Trả về tất cả sách
            } else {
                return bookRepository.findReadyBooksByCategory(query, categoryId); // Trả về sách theo category
            }

    }

    // Thống kê sách đang được mượn
    public List<Book> getDamagedBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear,
            Integer toMonth, Integer toYear, Integer categoryId) {

        // Nếu tất cả tháng, năm đều null
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null) {
            if (categoryId == null) {
                return bookRepository.findAllDamagedBooks(query); // Trả về tất cả sách
            } else {
                return bookRepository.findDamagedBooksByCategory(query, categoryId); // Trả về sách theo category
            }
        }

        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null
        	|| fromMonth == null && fromYear == null && toMonth != null && toYear != null) {
        	return bookRepository.findDamagedBooksByMonthAndYear(query, fromMonth, fromYear, categoryId, categoryId);
        }


        // Nếu tháng, năm được cung cấp nhưng không có categoryId
        if (categoryId == null) {
            return bookRepository.findDamagedBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }

        // Nếu tất cả đều được cung cấp (có cả categoryId và khoảng thời gian)
        return bookRepository.findDamagedBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
    }

}

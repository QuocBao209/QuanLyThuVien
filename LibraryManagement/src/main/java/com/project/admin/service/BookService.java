package com.project.admin.service;

import com.project.admin.entity.Author;
import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public List<Book> getBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllBooks(query);
        }
        
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByCategory(query, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBooksByYear(query, fromYear);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBooksByMonthAndYear(query, fromMonth, fromYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        return bookRepository.findAllBooks(query);
    }

    // Borrowing
    public List<Book> getBorrowingBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllBorrowedBooks(query);
        }
        
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByCategory(query, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBorrowedBooksByYear(query, fromYear);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findBorrowedBooksByMonthAndYear(query, fromMonth, fromYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findBorrowedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findBorrowedBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findBorrowedBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBorrowedBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findBorrowedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        return bookRepository.findAllBorrowedBooks(query);
    }

    // Ready
    public List<Book> getReadyBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {

        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllReadyBooks(query);
        }
        
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findReadyBooksByCategory(query, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findReadyBooksByYear(query, fromYear);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findReadyBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findReadyBooksByMonthAndYear(query, fromMonth, fromYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findReadyBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findReadyBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findReadyBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findReadyBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findReadyBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        return bookRepository.findAllReadyBooks(query);
    }

    // Damaged
    public List<Book> getDamagedBooksByMonthAndYear(String query, Integer fromMonth, Integer fromYear, 
            Integer toMonth, Integer toYear, Integer categoryId) {
    	
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findAllDamagedBooks(query);
        }
        
        if (fromMonth == null && fromYear == null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findDamagedBooksByCategory(query, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findDamagedBooksByYear(query, fromYear);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findDamagedBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId == null) {
            return bookRepository.findDamagedBooksByMonthAndYear(query, fromMonth, fromYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null && categoryId != null) {
            return bookRepository.findDamagedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId == null) {
            return bookRepository.findDamagedBooksByDateRange(query, fromMonth, fromYear, toMonth, toYear);
        }
        
        if (fromMonth != null && fromYear != null && toMonth != null && toYear != null && categoryId != null) {
            return bookRepository.findDamagedBooksByDateRangeAndCategory(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        }
        
        if (fromMonth == null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findDamagedBooksByYearAndCategory(query, fromYear, categoryId);
        }
        
        if (fromMonth != null && fromYear != null && toMonth == null && toYear == null) {
            return bookRepository.findDamagedBooksByMonthAndYearAndCategory(query, fromMonth, fromYear, categoryId);
        }
        
        return bookRepository.findAllDamagedBooks(query);
    }

    public long getTotalBooks() {
        return bookRepository.findByIsDeletedFalse().stream().mapToLong(Book::getAmount).sum();
    }

    public long getTotalBorrowing(Integer month, Integer year) {
        List<Book> books = (month != null && year != null) 
            ? bookRepository.findBooksByMonthAndYear(null, month, year)
            : bookRepository.findByIsDeletedFalse();
        return books.stream().mapToLong(Book::getBorrowedRecordsCount).sum();
    }

    public long getTotalAvailable(Integer month, Integer year) {
        List<Book> books = (month != null && year != null) 
            ? bookRepository.findBooksByMonthAndYear(null, month, year)
            : bookRepository.findByIsDeletedFalse();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        return totalAvailable < 0 ? 0 : totalAvailable;
    }
    
// // Phương thức mới: getBooksForStatistics với startDate và endDate kiểu Date
//    public List<Book> getBooksForStatistics(String query, Date startDate, Date endDate, String categoryId, String statisticType) {
//        // Bước 1: Lấy tất cả sách chưa bị xóa
//        List<Book> books = bookRepository.findByIsDeletedFalse();
//
//        // Bước 2: Lọc theo query (nếu có)
//        if (query != null && !query.trim().isEmpty()) {
//            String keyword = query.trim().toLowerCase();
//            books = books.stream()
//                    .filter(book -> 
//                            book.getBookName().toLowerCase().contains(keyword) ||
//                            book.getAuthors().stream()
//                                    .anyMatch(author -> author.getAuthorName().toLowerCase().contains(keyword)))
//                    .collect(Collectors.toList());
//        }
//
//        // Bước 3: Lọc theo categoryId (nếu có)
//        if (categoryId != null && !categoryId.trim().isEmpty()) {
//            books = books.stream()
//                    .filter(book -> book.getCategory() != null && categoryId.equals(String.valueOf(book.getCategory().getCategoryId())))
//                    .collect(Collectors.toList());
//        }
//
//        // Bước 4: Kiểm tra và chuẩn hóa endDate
//        if (startDate == null || endDate == null) {
//            throw new IllegalArgumentException("startDate và endDate không được null");
//        }
//        // Tạo một bản sao của endDate và điều chỉnh thành ngày cuối cùng của tháng
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(endDate);
//        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
//        Date finalEndDate = cal.getTime(); // Biến này là effectively final
//
//        // Bước 5: Tính toán số liệu thống kê dựa trên khoảng thời gian
//        for (Book book : books) {
//            // Tính borrowCount: Số bản ghi mượn trong khoảng thời gian với status = "borrowed"
//            long borrowCount = book.getBorrowReturns().stream()
//                    .filter(br -> br.getStartDate() != null && 
//                                  !br.getStartDate().before(startDate) && 
//                                  !br.getStartDate().after(finalEndDate) && 
//                                  "borrowed".equals(br.getStatus()))
//                    .count();
//            book.setBorrowCount((int) borrowCount);
//
//            // Tính isDamaged: Số bản ghi hư hại trong khoảng thời gian với status = "damaged"
//            long damagedCount = book.getBorrowReturns().stream()
//                    .filter(br -> br.getStartDate() != null && 
//                                  !br.getStartDate().before(startDate) && 
//                                  !br.getStartDate().after(finalEndDate) && 
//                                  "damaged".equals(br.getStatus()))
//                    .count();
//            book.setIsDamaged((int) damagedCount);
//        }
//
//        // Bước 6: Nếu statisticType yêu cầu thêm xử lý, có thể mở rộng ở đây
//        if ("book-borrow-stats".equals(statisticType)) {
//            // Hiện tại đã tính borrowCount và isDamaged, có thể thêm logic khác nếu cần
//        }
//
//        return books;
//    }

}
package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.Borrow_ReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookBorrowStatsService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Borrow_ReturnRepository borrowReturnRepository;

    // Lấy số sách đang mượn theo thể loại
    public Map<String, Integer> getBorrowStatsByCategory(Integer month, Integer year) {
        List<Borrow_Return> borrowReturns = borrowReturnRepository.findByStatusIn(List.of("borrowed")); // Chỉ lấy sách đang mượn

        if (month != null && year != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1, 0, 0, 0); // Đầu tháng
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.set(year, month, 1, 0, 0, 0); // Đầu tháng sau

            borrowReturns = borrowReturns.stream()
                    .filter(br -> {
                        Calendar startCal = Calendar.getInstance();
                        startCal.setTime(br.getStartDate());
                        return !startCal.before(calendar) && startCal.before(endCalendar);
                    })
                    .toList();
        }

        // Thống kê số sách đang mượn theo thể loại
        Map<String, Integer> statsByCategory = new HashMap<>();
        for (Borrow_Return br : borrowReturns) {
            String categoryName = br.getBook().getCategory().getCategoryName();
            statsByCategory.put(categoryName, statsByCategory.getOrDefault(categoryName, 0) + 1);
        }

        return statsByCategory;
    }

    // Lấy tổng số sách
    public int getTotalBooks() {
        return bookRepository.findByIsDeletedFalse()
                .stream()
                .mapToInt(Book::getAmount)
                .sum();
    }

    // Lấy số sách đang mượn
    public int getTotalBorrowing(Integer month, Integer year) {
        List<Borrow_Return> borrowReturns = borrowReturnRepository.findByStatusIn(List.of("borrowed"));

        if (month != null && year != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1, 0, 0, 0);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.set(year, month, 1, 0, 0, 0);

            borrowReturns = borrowReturns.stream()
                    .filter(br -> {
                        Calendar startCal = Calendar.getInstance();
                        startCal.setTime(br.getStartDate());
                        return !startCal.before(calendar) && startCal.before(endCalendar);
                    })
                    .toList();
        }

        return borrowReturns.size();
    }

    // Lấy số sách tồn kho
    public int getTotalAvailable(Integer month, Integer year) {
        int totalBooks = getTotalBooks();
        int totalBorrowing = getTotalBorrowing(month, year);
        int totalDamaged = getTotalDamaged();
        return totalBooks - totalBorrowing - totalDamaged;
    }

    // Lấy tổng số sách bị hư hại
    public int getTotalDamaged() {
        return bookRepository.findByIsDeletedFalse()
                .stream()
                .mapToInt(Book::getIsDamaged)
                .sum();
    }

    // Lấy tổng số sách theo thể loại
    public Map<String, Integer> getTotalBooksByCategory() {
        List<Book> books = bookRepository.findByIsDeletedFalse();
        return books.stream()
                .collect(Collectors.groupingBy(
                        book -> book.getCategory().getCategoryName(),
                        Collectors.summingInt(Book::getAmount)
                ));
    }

    // Lấy số sách hư hại theo thể loại
    public Map<String, Integer> getDamagedBooksByCategory() {
        List<Book> books = bookRepository.findByIsDeletedFalse();
        return books.stream()
                .collect(Collectors.groupingBy(
                        book -> book.getCategory().getCategoryName(),
                        Collectors.summingInt(Book::getIsDamaged)
                ));
    }
    
    // Tổng số sách theo thể loại
    public int getTotalBooksByCategory(Integer categoryId) {
        if (categoryId == null) {
            return (int) bookRepository.count(); // Nếu không chọn thể loại, trả về tổng số sách
        }
        return bookRepository.countBooksByCategory(categoryId); // Phương thức đếm sách theo thể loại
    }

    // Số sách đang mượn theo thể loại và tháng/năm
    public int getTotalBorrowingByCategory(Integer categoryId, Integer month, Integer year) {
        return borrowReturnRepository.countBorrowingBooksByCategory(categoryId, month, year); // Phương thức đếm sách đang mượn theo thể loại và thời gian
    }

    // Số sách sẵn sàng theo thể loại và tháng/năm
    public int getTotalAvailableByCategory(Integer categoryId, Integer month, Integer year) {
        return borrowReturnRepository.countAvailableBooksByCategory(categoryId, month, year); // Phương thức đếm sách sẵn sàng
    }

    // Số sách bị hư hỏng theo thể loại
    public int getTotalDamagedByCategory(Integer categoryId) {
        return bookRepository.countDamagedBooksByCategory(categoryId); // Phương thức đếm sách bị hư hỏng
    }
}

package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.Borrow_ReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookBorrowStatsService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private Borrow_ReturnRepository borrowReturnRepository;

    public Map<String, Integer> getBorrowStatsByCategory(Integer month, Integer year) {
        // Lấy tất cả Borrow_Return với status "borrowed"
        List<Borrow_Return> borrowReturns = borrowReturnRepository.findByStatusIn(List.of("borrowed", "returned", "outdate"));

        // Lọc theo thời gian nếu có month và year
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

        // Tính số lượng sách mượn theo thể loại
        Map<String, Integer> statsByCategory = new HashMap<>();
        for (Borrow_Return br : borrowReturns) {
            String categoryName = br.getBook().getCategory().getCategoryName();
            statsByCategory.put(categoryName, statsByCategory.getOrDefault(categoryName, 0) + 1);
        }

        return statsByCategory;
    }

    public int getTotalBooks() {
        // Tổng số sách (amount) của tất cả sách chưa bị xóa
        return bookRepository.findByIsDeletedFalse()
                .stream()
                .mapToInt(Book::getAmount)
                .sum();
    }

    public int getTotalAvailable(Integer month, Integer year) {
        // Tổng số sách
        int totalBooks = getTotalBooks();

        // Số sách đang được mượn
        int totalBorrowed = getTotalBorrowed(month, year);

        // Số sách bị hư/mất
        int totalDamaged = getTotalDamaged();

        // Số sách tồn kho = Tổng số sách - Số sách đang mượn - Số sách bị hư/mất
        return totalBooks - totalBorrowed - totalDamaged;
    }

    public int getTotalBorrowed(Integer month, Integer year) {

        List<Borrow_Return> borrowReturns = borrowReturnRepository.findByStatusIn(List.of("borrowed", "returned", "outdate"));

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

        // Đếm số lượng sách phù hợp
        return borrowReturns.size();
    }


    public int getTotalDamaged() {
        // Tổng số sách bị hư/mất (isDamaged) của tất cả sách chưa bị xóa
        return bookRepository.findByIsDeletedFalse()
                .stream()
                .mapToInt(Book::getIsDamaged)
                .sum();
    }
}

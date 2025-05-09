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
    
}
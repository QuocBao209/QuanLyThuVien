package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.Borrow_ReturnRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Borrow_ReturnService {
    private final Borrow_ReturnRepository borrowReturnRepository;
    private final BookRepository bookRepository;
    

    public Borrow_ReturnService(Borrow_ReturnRepository borrowReturnRepository, BookRepository bookRepository) {
        this.borrowReturnRepository = borrowReturnRepository;
		this.bookRepository = bookRepository;
    }

    public List<Borrow_Return> getBorrowReturns() {
        return borrowReturnRepository.findAll();
    }

    public Borrow_Return findById(Long id) {
        return borrowReturnRepository.findById(id).orElse(null);
    }

    public void save(Borrow_Return borrowReturn) {
        borrowReturnRepository.save(borrowReturn);
    }

    public void transferData(List<Borrow_Return> borrowReturns) {
        borrowReturnRepository.saveAll(borrowReturns);
    }
    
    // Lấy danh sách các sách mượn/trả đúng đối tượng User
    public List<Borrow_Return> findByUser_UserId(Long userId) {
        return borrowReturnRepository.findByUser_UserId(userId);
    }
    
    // Lấy danh sách các sách chỉ khi User bấm Xác nhận mượn
    public List<Borrow_Return> getConfirmedBorrowReturns() {
        return borrowReturnRepository.findByUserConfirmDateIsNotNull();
    }

    public int getTotalBooks() {

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
        List<Borrow_Return> borrowReturns = getConfirmedBorrowReturns();

        if (month != null && year != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1, 0, 0, 0); // Đầu tháng
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.set(year, month, 1, 0, 0, 0); // Đầu tháng sau

            borrowReturns = borrowReturns.stream()
                    .filter(br -> {
                        Calendar confirmCal = Calendar.getInstance();
                        confirmCal.setTime(br.getUserConfirmDate());
                        return !confirmCal.before(calendar) && confirmCal.before(endCalendar);
                    })
                    .collect(Collectors.toList());
        }

        return borrowReturns.size();
    }

    public int getTotalDamaged() {

        return bookRepository.findByIsDeletedFalse()
                .stream()
                .mapToInt(Book::getIsDamaged)
                .sum();
    }
    
    public List<Borrow_Return> searchByBookOrAuthor(String keyword) {
        return borrowReturnRepository.searchByBookOrAuthor(keyword);
    }
}
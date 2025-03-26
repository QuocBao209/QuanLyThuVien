package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.Borrow_ReturnRepository;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Phương thức từ BookBorrowStatsService
    public Map<String, Integer> getBorrowStatsByCategory(Integer month, Integer year) {
        // Lấy tất cả Borrow_Return đã xác nhận (userConfirmDate không null)
        List<Borrow_Return> borrowReturns = getConfirmedBorrowReturns();

        // Lọc theo thời gian nếu có month và year
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

        // Đếm số lượng sách đang được mượn
        return borrowReturns.size();
    }

    public int getTotalDamaged() {
        // Tổng số sách bị hư/mất (isDamaged) của tất cả sách chưa bị xóa
        return bookRepository.findByIsDeletedFalse()
                .stream()
                .mapToInt(Book::getIsDamaged)
                .sum();
    }

//    public List<Borrow_Return> getStatsByMonthYear(int month, int year) {
//        // Lấy tất cả Borrow_Return đã xác nhận (userConfirmDate không null)
//        List<Borrow_Return> borrowReturns = getConfirmedBorrowReturns();
//
//        // Lọc theo tháng và năm dựa trên userConfirmDate
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month - 1, 1, 0, 0, 0); // Đầu tháng
//        Calendar endCalendar = Calendar.getInstance();
//        endCalendar.set(year, month, 1, 0, 0, 0); // Đầu tháng sau
//
//        borrowReturns = borrowReturns.stream()
//                .filter(br -> {
//                    Calendar confirmCal = Calendar.getInstance();
//                    confirmCal.setTime(br.getUserConfirmDate());
//                    return !confirmCal.before(calendar) && confirmCal.before(endCalendar);
//                })
//                .collect(Collectors.toList());
//
//        // Tạo Map để đếm số lượng mượn theo book
//        Map<Long, Integer> borrowCountMap = new HashMap<>();
//        for (Borrow_Return br : borrowReturns) {
//            Long bookId = br.getBook().getBookId();
//            borrowCountMap.put(bookId, borrowCountMap.getOrDefault(bookId, 0) + 1);
//        }
//
//        // Lấy tất cả sách chưa bị xóa
//        List<Book> books = bookRepository.findByIsDeletedFalse();
//        List<Borrow_Return> stats = new ArrayList<>();
//
//        // Chuyển đổi sang BookBorrowStats
//        for (Book book : books) {
//            Long bookId = book.getBookId();
//            int borrowCount = borrowCountMap.getOrDefault(bookId, 0);
//
//            Borrow_Return stat = new Borrow_Return();
//            stat.setId(bookId);
//            stat.setBook(book.getBookName());
//            stat.setBook(book.getAuthors().isEmpty() ? "Unknown Author" : book.getAuthors().get(0).getAuthorName());
//            stat.setBook(book.getCategory().getCategoryName());
//            stat.setBookCover(book.getBookImage() != null ? book.getBookImage() : "default.jpg");
//            stat.setBorrowCount(borrowCount);
//            stat.setBorrowAvailable(book.getAmount() - borrowCount - book.getIsDamaged());
//
//            stats.add(stat);
//        }
//
//        return stats;
//    }
}
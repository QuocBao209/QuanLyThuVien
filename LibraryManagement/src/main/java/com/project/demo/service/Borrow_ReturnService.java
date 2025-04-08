package com.project.demo.service;

import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.User;
import com.project.demo.repository.Borrow_ReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class Borrow_ReturnService {
    @Autowired private Borrow_ReturnRepository borrowReturnRepository;

    public List<Borrow_Return> getBorrowReturns() {
        return borrowReturnRepository.findAll();
    }

    public void transferData(List<Borrow_Return> borrowReturns) {
        borrowReturnRepository.saveAll(borrowReturns);
    }

    // Xử lý sự kiện mượn sách ( bookFilter - account )
    public void saveBorrow(Borrow_Return borrow) {
        borrowReturnRepository.save(borrow);
    }

    // Xử lý sự kiện mượn sách ( bookFilter - account )
    public List<Borrow_Return> getBorrowsByUser(User user) {
        return borrowReturnRepository.findByUser(user);
    }

    // Đếm số lượt mượn sách đang hoạt động của người dùng
    public int countActiveBorrowSessionsByUser(Long userId) {
        return borrowReturnRepository.countActiveBorrowSessionsByUser(userId);
    }
    public List<Borrow_Return> findBorrowHistory(Long userId, String keyword, String status) {
        List<Borrow_Return> allBorrows = borrowReturnRepository.findByUser_UserId(userId);

        return allBorrows.stream()
                .filter(br -> {
                    boolean matchStatus = (status == null || status.isEmpty()) || br.getStatus().equalsIgnoreCase(status);
                    boolean matchKeyword = (keyword == null || keyword.isEmpty())
                            || (br.getBook() != null &&
                            br.getBook().getBookName().toLowerCase().contains(keyword.toLowerCase()));
                    return matchStatus && matchKeyword;
                })
                .sorted(Comparator
                        .comparing((Borrow_Return br) -> br.getUserConfirmDate() == null ? 0 : 1)
                        .thenComparing(br -> getStatusOrder(br.getStatus())))
                .collect(Collectors.toList());
    }


    private int getStatusOrder(String status) {
        switch (status) {
            case "pending": return 1;
            case "borrowed": return 2;
            case "returned": return 3;
            case "outdate": return 4;
            default: return 5;
        }
    }

}


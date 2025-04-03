package com.project.demo.service;

import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.User;
import com.project.demo.repository.Borrow_ReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}


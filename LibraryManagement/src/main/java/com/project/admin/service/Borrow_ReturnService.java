package com.project.admin.service;

import com.project.admin.entity.Borrow_Return;
import com.project.admin.repository.Borrow_ReturnRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Borrow_ReturnService {
    private final Borrow_ReturnRepository borrowReturnRepository;

    public Borrow_ReturnService(Borrow_ReturnRepository borrowReturnRepository) {
        this.borrowReturnRepository = borrowReturnRepository;
    }

    public List<Borrow_Return> getBorrowReturns() {
        return borrowReturnRepository.findAll();
    }

    public void transferData(List<Borrow_Return> borrowReturns) {
        borrowReturnRepository.saveAll(borrowReturns);
    }
}

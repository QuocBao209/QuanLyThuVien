package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.ImportDetail;
import com.project.admin.entity.ImportReceipt;
import com.project.admin.repository.ImportDetailRepository;
import com.project.admin.repository.ImportReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportDetailRepository importDetailRepository;
    private final BookService bookService;

    @Transactional
    public void importBooks(List<ImportDetail> importDetails, LocalDate importDate) {
        ImportReceipt receipt = new ImportReceipt();
        receipt.setImportDate(importDate);
        importReceiptRepository.save(receipt);

        importDetails.forEach(detail -> {
            detail.setImportReceipt(receipt);
            importDetailRepository.save(detail);

            // Cập nhật số lượng sách trong kho
            Book book = detail.getBook();
            book.setAmount(book.getAmount() + detail.getAmount());
            bookService.saveBook(book);
        });
    }

    // Lấy tất cả ImportReceipt
    public List<ImportReceipt> getAllImportReceipts() {
        return importReceiptRepository.findAll();
    }

    // Lấy tất cả ImportDetail
    public List<ImportDetail> getAllImportDetails() {
        return importDetailRepository.findAll();
    }
    @Transactional
    public void transferImportReceipts(List<ImportReceipt> importReceipts) {
        if (importReceipts != null && !importReceipts.isEmpty()) {
            importReceiptRepository.saveAll(importReceipts);
        }
    }

    @Transactional
    public void transferImportDetails(List<ImportDetail> importDetails) {
        if (importDetails != null && !importDetails.isEmpty()) {
            importDetailRepository.saveAll(importDetails);
        }
    }


}

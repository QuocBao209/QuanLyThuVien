package com.project.admin.service;

import com.project.admin.entity.Book;
import com.project.admin.entity.ImportDetail;
import com.project.admin.entity.ImportReceipt;
import com.project.admin.entity.User;
import com.project.admin.repository.ImportDetailRepository;
import com.project.admin.repository.ImportReceiptRepository;
import com.project.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportDetailRepository importDetailRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    @Transactional
    public void importBooks(List<ImportDetail> importDetails, LocalDate importDate, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        ImportReceipt receipt = importReceiptRepository.findByImportDate(importDate)
                .orElseGet(() -> {
                    ImportReceipt newReceipt = new ImportReceipt();
                    newReceipt.setImportDate(importDate);
                    newReceipt.setUser(user);
                    return importReceiptRepository.save(newReceipt);
                });

        Map<Book, Integer> bookAmountMap = new HashMap<>();

        for (ImportDetail detail : importDetails) {
            Optional<ImportDetail> existingDetail = importDetailRepository.findByImportReceiptAndBook(receipt, detail.getBook());

            if (existingDetail.isPresent()) {
                ImportDetail foundDetail = existingDetail.get();
                foundDetail.setAmount(foundDetail.getAmount() + detail.getAmount());
                importDetailRepository.save(foundDetail);
            } else {
                detail.setImportReceipt(receipt);
                importDetailRepository.save(detail);
            }

            bookAmountMap.merge(detail.getBook(), detail.getAmount(), Integer::sum);
        }

        for (Map.Entry<Book, Integer> entry : bookAmountMap.entrySet()) {
            Book book = entry.getKey();
            book.setAmount(book.getAmount() + entry.getValue());
            bookService.save(book);
        }
    }
    public List<ImportReceipt> getAllImportReceipts() {
        return importReceiptRepository.findAll();
    }

    public List<ImportDetail> getAllImportDetails() {
        return importDetailRepository.findAll();
    }

    public List<ImportDetail> getImportDetailsByInvoiceId(String invoiceId) {
        return importDetailRepository.getImportDetailsByInvoiceId(invoiceId);
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
    
    public List<ImportDetail> searchByBookOrAuthor(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return importDetailRepository.findAll();
        }
        return importDetailRepository.findByBookNameOrAuthorContainingIgnoreCase(keyword);
    }
    
    public List<ImportDetail> searchByBookOrAuthorAndDate(String keyword, LocalDate importDate) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return importDetailRepository.findByImportDate(importDate);
        }
        return importDetailRepository.findByBookNameOrAuthorContainingIgnoreCaseAndImportDate(keyword, importDate);
    }

    public List<ImportReceipt> searchImportReceipts(String keyword, LocalDate importDate) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (importDate == null) {
                return importReceiptRepository.findAll();
            }
            return importReceiptRepository.searchingByImportDate(importDate);
        }
        if (importDate == null) {
            return importReceiptRepository.findByInvoiceIdContainingOrUserNameContaining(keyword);
        }
        return importReceiptRepository.findByInvoiceIdContainingOrUserNameContainingAndImportDate(keyword, importDate);
    }
}

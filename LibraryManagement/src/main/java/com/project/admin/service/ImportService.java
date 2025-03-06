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
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final ImportReceiptRepository importReceiptRepository;
    private final ImportDetailRepository importDetailRepository;
    private final BookService bookService;

    @Transactional
    public void importBooks(List<ImportDetail> importDetails, LocalDate importDate) {
        ImportReceipt receipt = importReceiptRepository.findByImportDate(importDate)
                .orElseGet(() -> {
                    ImportReceipt newReceipt = new ImportReceipt();
                    newReceipt.setImportDate(importDate);
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
            bookService.saveBook(book);
        }
    }

    public List<ImportReceipt> getAllImportReceipts() {
        return importReceiptRepository.findAll();
    }

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

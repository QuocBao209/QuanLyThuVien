package com.project.admin.repository;

import com.project.admin.entity.ImportDetail;
import com.project.admin.entity.ImportReceipt;
import com.project.admin.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImportDetailRepository extends JpaRepository<ImportDetail, Long> {
    Optional<ImportDetail> findByImportReceiptAndBook(ImportReceipt importReceipt, Book book);
}

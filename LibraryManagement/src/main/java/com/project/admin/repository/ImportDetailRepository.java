package com.project.admin.repository;

import com.project.admin.entity.ImportDetail;
import com.project.admin.entity.ImportReceipt;
import com.project.admin.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImportDetailRepository extends JpaRepository<ImportDetail, Long> {
    Optional<ImportDetail> findByImportReceiptAndBook(ImportReceipt importReceipt, Book book);

    @Query("SELECT id FROM ImportDetail id WHERE id.importReceipt.invoiceId = :invoiceId")
    List<ImportDetail> getImportDetailsByInvoiceId(@Param("invoiceId") String invoiceId);
}

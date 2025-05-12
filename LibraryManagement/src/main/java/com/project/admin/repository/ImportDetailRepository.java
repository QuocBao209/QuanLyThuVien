package com.project.admin.repository;

import com.project.admin.entity.ImportDetail;
import com.project.admin.entity.ImportReceipt;
import com.project.admin.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ImportDetailRepository extends JpaRepository<ImportDetail, Long> {
    Optional<ImportDetail> findByImportReceiptAndBook(ImportReceipt importReceipt, Book book);

    @Query("SELECT id FROM ImportDetail id WHERE id.importReceipt.invoiceId = :invoiceId")
    List<ImportDetail> getImportDetailsByInvoiceId(@Param("invoiceId") String invoiceId);
    
    @Query("SELECT i FROM ImportDetail i WHERE LOWER(i.book.bookName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR EXISTS (SELECT a FROM i.book.authors a WHERE LOWER(a.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
     List<ImportDetail> findByBookNameOrAuthorContainingIgnoreCase(@Param("keyword") String keyword);
    
    @Query("SELECT i FROM ImportDetail i WHERE i.importReceipt.importDate = :importDate")
    List<ImportDetail> findByImportDate(@Param("importDate") LocalDate importDate);

    @Query("SELECT i FROM ImportDetail i WHERE (LOWER(i.book.bookName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR EXISTS (SELECT a FROM i.book.authors a WHERE LOWER(a.authorName) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "AND i.importReceipt.importDate = :importDate")
    List<ImportDetail> findByBookNameOrAuthorContainingIgnoreCaseAndImportDate(@Param("keyword") String keyword,
                                                                              @Param("importDate") LocalDate importDate);
}

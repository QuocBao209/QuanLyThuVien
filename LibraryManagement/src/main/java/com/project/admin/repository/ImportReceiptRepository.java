package com.project.admin.repository;

import com.project.admin.entity.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
	Optional<ImportReceipt> findByImportDate(LocalDate importDate);

    @Query("SELECT ir FROM ImportReceipt ir WHERE ir.importDate = :importDate")
    List<ImportReceipt> searchingByImportDate(@Param("importDate") LocalDate importDate);

    @Query("SELECT ir FROM ImportReceipt ir WHERE CAST(ir.invoiceId AS string) LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(ir.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
     List<ImportReceipt> findByInvoiceIdContainingOrUserNameContaining(@Param("keyword") String keyword);

     @Query("SELECT ir FROM ImportReceipt ir WHERE (CAST(ir.invoiceId AS string) LIKE CONCAT('%', :keyword, '%') " +
            "OR LOWER(ir.user.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND ir.importDate = :importDate")
     List<ImportReceipt> findByInvoiceIdContainingOrUserNameContainingAndImportDate(@Param("keyword") String keyword,
                                                                                   @Param("importDate") LocalDate importDate);
}

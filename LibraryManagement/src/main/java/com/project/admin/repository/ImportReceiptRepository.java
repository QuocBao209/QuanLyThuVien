package com.project.admin.repository;

import com.project.admin.entity.ImportReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {
    Optional<ImportReceipt> findByImportDate(LocalDate importDate);
}

package com.project.admin.repository;

import com.project.admin.entity.Borrow_Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Borrow_ReturnRepository extends JpaRepository<Borrow_Return, Long> {
    List<Borrow_Return> findByStatus(String status);
    
    List<Borrow_Return> findByUserConfirmDateIsNotNull();
    
    List<Borrow_Return> findByUser_UserId(Long userId);

    @Query("SELECT br FROM Borrow_Return br WHERE br.status = :status AND MONTH(br.startDate) = :month AND YEAR(br.startDate) = :year")
    List<Borrow_Return> findByStatusAndMonthYear(@Param("status") String status, @Param("month") Integer month, @Param("year") Integer year);
}

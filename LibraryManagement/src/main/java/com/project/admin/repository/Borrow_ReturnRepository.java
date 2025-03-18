package com.project.admin.repository;

import com.project.admin.entity.Borrow_Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Borrow_ReturnRepository extends JpaRepository<Borrow_Return, Long> {
    List<Borrow_Return> findByStatus(String status);
    
    List<Borrow_Return> findByUserConfirmDateIsNotNull();
    
    List<Borrow_Return> findByUser_UserId(Long userId);
}

package com.project.admin.repository;

import com.project.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAllByRole(String role); 
    List<User> findByRoleAndNameContainingOrRoleAndEmailContaining(String role1, String name, String role2, String email);

    // top user mượn sách nhiều nhất
    List<User> findByRoleOrderByBorrowCountDesc(String role);
   
    // Lấy danh sách người dùng có số lần vi phạm > 0
    @Query("SELECT u FROM User u WHERE u.violationCount > 0")
    List<User> findUsersWithViolations();
    
    // Tìm kiếm người dùng có vi phạm theo tên hoặc email
    @Query("SELECT u FROM User u WHERE u.violationCount > 0 AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchUsersWithViolations(String keyword);


    boolean existsByEmail(String email);

    // Lấy tất cả danh sách khi không nhập Tháng/Năm
    @Query("SELECT DISTINCT u FROM User u " +
    	       "JOIN Borrow_Return br ON u.id = br.user.id " +
    	       "WHERE u.role = 'USER' " +
    	       "AND (:query IS NULL OR u.name LIKE %:query%) " +
    	       "ORDER BY u.borrowCount DESC")
    List<User> findByRoleAndUserNameContainingOrderByBorrowCountDesc( String query);

    
    // Thống kê độc giả mượn theo khoảng thời gian từ Tháng/Năm đến Tháng/Năm
    @Query("SELECT u FROM User u JOIN Borrow_Return br ON u.id = br.user.id " +
           "WHERE (:query IS NULL OR u.name LIKE %:query%) " +
           "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
           "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth))" +
           "ORDER BY u.borrowCount DESC")
    List<User> findUsersByDateRange(String query, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear);

}



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
    List<User> findAllByRole(String role); // Sửa lại
    List<User> findByRoleAndNameContainingOrRoleAndEmailContaining(String role1, String name, String role2, String email);
    
    // Lấy danh sách người dùng có số lần vi phạm > 0
    @Query("SELECT u FROM User u WHERE u.violationCount > 0")
    List<User> findUsersWithViolations();
    
    // Tìm kiếm người dùng có vi phạm theo tên hoặc email
    @Query("SELECT u FROM User u WHERE u.violationCount > 0 AND (LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<User> searchUsersWithViolations(String keyword);
}



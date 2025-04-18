package com.project.demo.repository;

import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface Borrow_ReturnRepository extends JpaRepository<Borrow_Return, Long> {

	// Lấy danh sách mượn theo đối tượng User
	List<Borrow_Return> findByUser(User user);

	// Lấy danh sách mượn theo userId
	List<Borrow_Return> findByUser_UserId(Long userId);

	// Đếm số lần mượn sách đang hoạt động
	@Query("SELECT COUNT(b.id) FROM Borrow_Return b " +
			"WHERE b.user.userId= :userId AND b.status IN ('borrowed', 'pending')")
	int countActiveBorrowSessionsByUser(@Param("userId") Long userId);

	long countByUserAndStatus(User user, String status);
}




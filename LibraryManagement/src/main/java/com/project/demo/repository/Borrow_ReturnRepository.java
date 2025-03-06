package com.project.demo.repository;

import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Borrow_ReturnRepository extends JpaRepository<Borrow_Return, Long> {
	
	// Xử lý sự kiện mượn sách ( bookFilter - account )
	List<Borrow_Return> findByUser(User user);
}


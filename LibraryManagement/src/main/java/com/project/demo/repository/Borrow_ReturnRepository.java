package com.project.demo.repository;

import com.project.demo.entity.Borrow_Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Borrow_ReturnRepository extends JpaRepository<Borrow_Return, Long> {
}


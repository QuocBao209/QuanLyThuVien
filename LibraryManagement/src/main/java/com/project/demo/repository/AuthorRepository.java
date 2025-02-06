package com.project.demo.repository;

import com.project.demo.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByName(String name);  // Tìm tác giả theo tên
}

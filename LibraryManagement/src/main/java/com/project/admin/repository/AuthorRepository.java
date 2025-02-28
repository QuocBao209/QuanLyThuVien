package com.project.admin.repository;

import com.project.admin.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findByAuthorName(String authorName);
}

package com.project.demo.repository;

import com.project.demo.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookTitleAndAuthor(String bookTitle, String authorName);  // Tìm sách theo tên và tác giả
}

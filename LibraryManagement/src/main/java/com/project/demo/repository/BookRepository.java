package com.project.demo.repository;

import com.project.demo.entity.Book;
import com.project.demo.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // Tìm sách theo tên sách và tác giả
    Optional<Book> findByBookNameAndAuthor(String bookName, Author author);

    // Tìm tất cả sách của một tác giả
    List<Book> findByAuthor(Author author);

    // Tìm tất cả sách theo tên sách
    List<Book> findByBookName(String bookName);
}

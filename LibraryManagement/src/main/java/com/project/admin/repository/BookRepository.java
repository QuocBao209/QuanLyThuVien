package com.project.admin.repository;

import com.project.admin.entity.Book;
import com.project.admin.entity.Author;
import com.project.admin.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByBookName(String bookName);

    List<Book> findByCategory(Category category);

    List<Book> findByPublishYear(int publishYear);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId")
    byte[] findBookImageById(@Param("bookId") Long bookId);
}

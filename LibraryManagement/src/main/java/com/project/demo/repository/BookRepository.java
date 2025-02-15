package com.project.demo.repository;

import com.project.demo.entity.Book;
import com.project.demo.entity.Author;
import com.project.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByBookNameAndAuthor(String bookName, Author author);

    List<Book> findByAuthor(Author author);

    List<Book> findByBookName(String bookName);

    List<Book> findByCategory(Category category);

    List<Book> findByPublishYear(int publishYear);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId")
    byte[] findBookImageById(@Param("bookId") Long bookId);
}

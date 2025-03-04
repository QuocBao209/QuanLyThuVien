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

    List<Book> findByBookNameAndIsDeletedFalse(String bookName);

    List<Book> findByCategoryAndIsDeletedFalse(Category category);

    List<Book> findByPublishYearAndIsDeletedFalse(int publishYear);

    List<Book> findByIsDeletedFalse();

    List<Book> findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(String title, String author);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId AND b.isDeleted = false")
    String findBookImagePathById(@Param("bookId") Long bookId);
}

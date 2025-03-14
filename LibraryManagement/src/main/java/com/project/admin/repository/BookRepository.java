package com.project.admin.repository;

import com.project.admin.entity.Book;
import com.project.admin.entity.Author;
import com.project.admin.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
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
    
    // Thống kế sách theo Tháng
    @Query("SELECT DISTINCT b FROM Book b " +
    	       "JOIN b.borrowReturns br " + 
    	       "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
    	       "AND (:month IS NULL OR FUNCTION('MONTH', br.startDate) = :month) " +
    	       "AND (:year IS NULL OR FUNCTION('YEAR', br.startDate) = :year)")
    	List<Book> findBooksByMonthAndYear(@Param("query") String query,
    	                                   @Param("month") Integer month,
    	                                   @Param("year") Integer year);

}

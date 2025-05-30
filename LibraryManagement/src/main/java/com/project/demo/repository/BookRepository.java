package com.project.demo.repository;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface BookRepository extends JpaRepository<Book, Long> {


    List<Book> findByIsDeletedFalse();


    // Phân trang danh sách theo Tác giả
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE a IN :authors AND b.bookId <> :excludeBookId")
    Page<Book> findBooksByAuthors(@Param("authors") List<Author> authors,
                                  @Param("excludeBookId") Long excludeBookId,
                                  Pageable pageable);
    // Sửa lại kiểu dữ liệu Page để phân trang
    @Query("SELECT b FROM Book b WHERE b.isDeleted = false " +
    	       "AND (:categoryNames IS NULL OR b.category.categoryName IN :categoryNames) " +
    	       "AND (:startYear IS NULL OR :endYear IS NULL OR (b.publishYear BETWEEN :startYear AND :endYear))")
    	Page<Book> findFilteredBooks(@Param("categoryNames") Set<String> categoryNames,
    	                             @Param("startYear") Integer startYear,
    	                             @Param("endYear") Integer endYear,
    	                             Pageable pageable);

    
    // Phân trang danh sách tất cả sách
    Page<Book> findByIsDeletedFalse(Pageable pageable);


    // Lấy danh sách sách có số lượt mượn nhiều
    @Query("SELECT b FROM Book b LEFT JOIN b.borrowReturns br " +
    	       "WHERE b.isDeleted = false " +
    	       "GROUP BY b " +
    	       "ORDER BY COUNT(br) DESC")
    	Page<Book> findBooksSortedByBorrowCount(Pageable pageable);
}

package com.project.demo.repository;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.Set;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByBookNameAndIsDeletedFalse(String bookName);

    List<Book> findByIsDeletedFalse();

    List<Book> findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(String title, String author);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId AND b.isDeleted = false")
    String findBookImagePathById(@Param("bookId") Long bookId);

    // Phân trang danh sách theo Tác giả
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE a IN :authors AND b.bookId <> :excludeBookId")
    Page<Book> findBooksByAuthors(@Param("authors") List<Author> authors,
                                  @Param("excludeBookId") Long excludeBookId,
                                  Pageable pageable);
    // Sửa lại kiểu dữ liệu Page để phân trang (Bảo)
    @Query("SELECT b FROM Book b WHERE b.isDeleted = false " +
    	       "AND (:categoryNames IS NULL OR b.category.categoryName IN :categoryNames) " +
    	       "AND (:startYear IS NULL OR :endYear IS NULL OR (b.publishYear BETWEEN :startYear AND :endYear))")
    	Page<Book> findFilteredBooks(@Param("categoryNames") Set<String> categoryNames,
    	                             @Param("startYear") Integer startYear,
    	                             @Param("endYear") Integer endYear,
    	                             Pageable pageable);

    
    // Phân trang danh sách tất cả sách (Bảo
    Page<Book> findByIsDeletedFalse(Pageable pageable);
}

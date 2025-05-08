package com.project.admin.repository;

import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {


    List<Book> findByIsDeletedFalse();

    List<Book> findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(String title, String author);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId AND b.isDeleted = false")
    String findBookImagePathById(@Param("bookId") Long bookId);

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false AND b.isDamaged > 0")
    List<Book> findByIsDeletedFalseAndIsDamagedGreaterThan(@Param("damage") int damage);

    // Thống kê sách theo Tháng
    @Query("SELECT DISTINCT b FROM Book b " +
    	       "JOIN b.borrowReturns br " +
    	       "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
    	       "AND (:fromMonth IS NULL OR FUNCTION('MONTH', br.startDate) = :fromMonth) " +
    	       "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) " +
    	       "AND (:toMonth IS NULL OR FUNCTION('MONTH', br.startDate) = :toMonth) " +
    	       "AND (:toYear IS NULL OR FUNCTION('YEAR', br.startDate) = :toYear)")
    	List<Book> findBooksByMonthAndYear(@Param("query") String query,
    	                                   @Param("fromMonth") Integer fromMonth,
    	                                   @Param("fromYear") Integer fromYear,
    	                                   @Param("toMonth") Integer toMonth,
    	                                   @Param("toYear") Integer toYear);
    
    // Lấy tất cả danh sách khi không nhập Tháng/Năm
    @Query("SELECT b FROM Book b WHERE (:query IS NULL OR b.bookName LIKE %:query%)")
    List<Book> findAllBooks(String query);

    // Thống kê sách theo khoảng thời gian từ Tháng/Năm đến Tháng/Năm
    @Query("SELECT b FROM Book b JOIN Borrow_Return br ON b.id = br.book.id " +
           "WHERE (:query IS NULL OR b.bookName LIKE %:query%) " +
           "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
           "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth))")
    List<Book> findBooksByDateRange(String query, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear);
    
    // Lấy tất cả sách theo thể loại
    @Query("SELECT b FROM Book b WHERE (:query IS NULL OR b.bookName LIKE %:query%) " +
    	       "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)")
    List<Book> findBooksByCategory(String query, Integer categoryId);

    // Thống kê sách theo thể loại + khoảng thời gian từ Tháng/Năm đến Tháng/Năm
    @Query("SELECT b FROM Book b JOIN Borrow_Return br ON b.id = br.book.id " +
    	       "WHERE (:query IS NULL OR b.bookName LIKE %:query%) " +
    	       "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) " +
    	       "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
    	       "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth))")
    List<Book> findBooksByDateRangeAndCategory(String query, Integer fromMonth, Integer fromYear, Integer toMonth, Integer toYear, 
    											Integer categoryId);
    
    // Đếm tổng số sách theo thể loại
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.categoryId = :categoryId")
    int countBooksByCategory(@Param("categoryId") Integer categoryId);

    // Đếm số sách bị hư hỏng theo thể loại
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.categoryId = :categoryId AND b.isDamaged > 0")
    int countDamagedBooksByCategory(@Param("categoryId") Integer categoryId);
}

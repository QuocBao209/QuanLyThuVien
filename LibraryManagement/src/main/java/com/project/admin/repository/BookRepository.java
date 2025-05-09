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

    List<Book> findByBookNameAndIsDeletedFalse(String bookName);

    List<Book> findByCategoryAndIsDeletedFalse(Category category);

    List<Book> findByPublishYearAndIsDeletedFalse(int publishYear);

    List<Book> findByIsDeletedFalse();

    List<Book> findByBookNameContainingIgnoreCaseAndIsDeletedFalseOrAuthors_AuthorNameContainingIgnoreCaseAndIsDeletedFalse(String title, String author);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId AND b.isDeleted = false")
    String findBookImagePathById(@Param("bookId") Long bookId);

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false AND b.isDamaged > 0")
    List<Book> findByIsDeletedFalseAndIsDamagedGreaterThan(@Param("damage") int damage);


// Tổng số sách
    // Lấy tất cả sách (không cần tháng/năm)
    @Query("SELECT b FROM Book b WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) AND b.isDeleted = false")
    List<Book> findAllBooks(@Param("query") String query);

    // Lấy tất cả sách theo thể loại
    @Query("SELECT b FROM Book b WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) AND b.isDeleted = false")
    List<Book> findBooksByCategory(@Param("query") String query, @Param("categoryId") Integer categoryId);

    // Lấy sách theo năm
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) AND b.isDeleted = false")
    List<Book> findBooksByYear(@Param("query") String query, @Param("fromYear") Integer fromYear);

    // Lấy sách theo năm và thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) AND b.isDeleted = false")
    List<Book> findBooksByYearAndCategory(@Param("query") String query, @Param("fromYear") Integer fromYear, 
                                          @Param("categoryId") Integer categoryId);

    // Lấy sách theo tháng và năm
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromMonth IS NULL OR FUNCTION('MONTH', br.startDate) = :fromMonth) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) AND b.isDeleted = false")
    List<Book> findBooksByMonthAndYear(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                       @Param("fromYear") Integer fromYear);

    // Lấy sách theo tháng, năm và thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromMonth IS NULL OR FUNCTION('MONTH', br.startDate) = :fromMonth) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) AND b.isDeleted = false")
    List<Book> findBooksByMonthAndYearAndCategory(@Param("query") String query, @Param("—fromMonth") Integer fromMonth, 
                                                  @Param("fromYear") Integer fromYear, @Param("categoryId") Integer categoryId);

    // Lấy sách theo khoảng thời gian
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
           "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth)) " +
           "AND b.isDeleted = false")
    List<Book> findBooksByDateRange(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                    @Param("fromYear") Integer fromYear, @Param("toMonth") Integer toMonth, 
                                    @Param("toYear") Integer toYear);

    // Lấy sách theo khoảng thời gian và thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) " +
           "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
           "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth)) " +
           "AND b.isDeleted = false")
    List<Book> findBooksByDateRangeAndCategory(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                               @Param("fromYear") Integer fromYear, @Param("toMonth") Integer toMonth, 
                                               @Param("toYear") Integer toYear, @Param("categoryId") Integer categoryId);

    
// Lọc sách đang mượn
    // Lấy tất cả sách đang mượn
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND b.isDeleted = false")
    List<Book> findAllBorrowedBooks(@Param("query") String query);

    // Lấy sách đang mượn theo thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) AND b.isDeleted = false")
    List<Book> findBorrowedBooksByCategory(@Param("query") String query, @Param("categoryId") Integer categoryId);

    // Lấy sách đang mượn theo năm
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) AND b.isDeleted = false")
    List<Book> findBorrowedBooksByYear(@Param("query") String query, @Param("fromYear") Integer fromYear);

    // Lấy sách đang mượn theo năm và thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) AND b.isDeleted = false")
    List<Book> findBorrowedBooksByYearAndCategory(@Param("query") String query, @Param("fromYear") Integer fromYear, 
                                                  @Param("categoryId") Integer categoryId);

    // Lấy sách đang mượn theo tháng và năm
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromMonth IS NULL OR FUNCTION('MONTH', br.startDate) = :fromMonth) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) AND b.isDeleted = false")
    List<Book> findBorrowedBooksByMonthAndYear(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                               @Param("fromYear") Integer fromYear);

    // Lấy sách đang mượn theo tháng, năm và thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:fromMonth IS NULL OR FUNCTION('MONTH', br.startDate) = :fromMonth) " +
           "AND (:fromYear IS NULL OR FUNCTION('YEAR', br.startDate) = :fromYear) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) AND b.isDeleted = false")
    List<Book> findBorrowedBooksByMonthAndYearAndCategory(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                                          @Param("fromYear") Integer fromYear, @Param("categoryId") Integer categoryId);

    // Lấy sách đang mượn theo khoảng thời gian
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
           "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth)) " +
           "AND b.isDeleted = false")
    List<Book> findBorrowedBooksByDateRange(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                            @Param("fromYear") Integer fromYear, @Param("toMonth") Integer toMonth, 
                                            @Param("toYear") Integer toYear);

    // Lấy sách đang mượn theo khoảng thời gian và thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.borrowReturns br " +
           "WHERE br.status = 'borrowed' AND (:query IS NULL OR LOWER(b.bookName) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId) " +
           "AND (YEAR(br.startDate) > :fromYear OR (YEAR(br.startDate) = :fromYear AND MONTH(br.startDate) >= :fromMonth)) " +
           "AND (YEAR(br.startDate) < :toYear OR (YEAR(br.startDate) = :toYear AND MONTH(br.startDate) <= :toMonth)) " +
           "AND b.isDeleted = false")
    List<Book> findBorrowedBooksByDateRangeAndCategory(@Param("query") String query, @Param("fromMonth") Integer fromMonth, 
                                                       @Param("fromYear") Integer fromYear, @Param("toMonth") Integer toMonth, 
                                                       @Param("toYear") Integer toYear, @Param("categoryId") Integer categoryId);
    
    
// Lọc sách đang sẵn sàng (ready)
    // Lọc không cần tháng/năm
    @Query("SELECT DISTINCT b FROM Book b " +
    	       "WHERE (b.amount - b.borrowCount - b.isDamaged) > 0  AND (:query IS NULL OR b.bookName LIKE %:query%)")
    	List<Book> findAllReadyBooks(@Param("query") String query);

    // Lọc theo thể loại
    @Query("SELECT DISTINCT b FROM Book b " +
    	       "WHERE (b.amount - b.borrowCount - b.isDamaged) > 0  " +
    	       "AND (:query IS NULL OR b.bookName LIKE %:query%) " +
    	       "AND (:categoryId IS NULL OR b.category.categoryId = :categoryId)")
    	List<Book> findReadyBooksByCategory(@Param("query") String query,
    	                                       @Param("categoryId") Integer categoryId);
}

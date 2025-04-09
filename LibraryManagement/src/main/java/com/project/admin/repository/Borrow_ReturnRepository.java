package com.project.admin.repository;

import com.project.admin.entity.Borrow_Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface Borrow_ReturnRepository extends JpaRepository<Borrow_Return, Long> {
    
    List<Borrow_Return> findByUserConfirmDateIsNotNull();
    
    List<Borrow_Return> findByUser_UserId(Long userId);

    List<Borrow_Return> findByStatusIn(List<String> statuses);
    
    // Đếm số sách đang mượn theo thể loại và tháng/năm
    @Query("SELECT COUNT(br) FROM Borrow_Return br JOIN br.book b WHERE b.category.categoryId = :categoryId " +
            "AND FUNCTION('MONTH', br.startDate) = :month AND FUNCTION('YEAR', br.startDate) = :year")
    int countBorrowingBooksByCategory(@Param("categoryId") Integer categoryId,
                                       @Param("month") Integer month, @Param("year") Integer year);

    // Đếm số sách sẵn sàng theo thể loại và tháng/năm
    @Query("SELECT COUNT(br) FROM Borrow_Return br JOIN br.book b WHERE b.category.categoryId = :categoryId " +
            "AND FUNCTION('MONTH', br.startDate) = :month AND FUNCTION('YEAR', br.startDate) = :year " +
            "AND br.status = 'AVAILABLE'")
    int countAvailableBooksByCategory(@Param("categoryId") Integer categoryId,
                                      @Param("month") Integer month, @Param("year") Integer year);
    
    // Lọc sách theo tên sách / tên tác giả (dùng trong BookController)
    @Query("SELECT br FROM Borrow_Return br " +
    	       "JOIN br.book b " +
    	       "LEFT JOIN b.authors a " +
    	       "WHERE LOWER(b.bookName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
    	       "   OR LOWER(a.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    	List<Borrow_Return> searchByBookOrAuthor(@Param("keyword") String keyword);

}

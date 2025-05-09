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
    
    // Lọc sách theo tên sách / tên tác giả (dùng trong BookController)
    @Query("SELECT br FROM Borrow_Return br " +
    	       "JOIN br.book b " +
    	       "LEFT JOIN b.authors a " +
    	       "WHERE LOWER(b.bookName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
    	       "   OR LOWER(a.authorName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    	List<Borrow_Return> searchByBookOrAuthor(@Param("keyword") String keyword);

    	
    //Lọc sách hư hại
    @Query("SELECT br FROM Borrow_Return br WHERE br.status = 'damaged_book' ")
    List<Borrow_Return> findDamagedBooks();
    
}

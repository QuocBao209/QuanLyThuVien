package com.project.demo.repository;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByBookName(String bookName);

    List<Book> findByCategory(Category category);

    List<Book> findByPublishYear(int publishYear);

    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId")
    String findBookImagePathById(@Param("bookId") Long bookId);

    // Phân trang
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE a IN :authors AND b.bookId <> :excludeBookId")
    Page<Book> findBooksByAuthors(@Param("authors") List<Author> authors,
                                  @Param("excludeBookId") Long excludeBookId,
                                  Pageable pageable);
}

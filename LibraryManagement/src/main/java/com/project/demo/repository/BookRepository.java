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

    // Tìm sách theo tên sách và tác giả
    Optional<Book> findByBookNameAndAuthor(String bookName, Author author);

    // Tìm tất cả sách của một tác giả
    List<Book> findByAuthor(Author author);

    // Tìm tất cả sách theo tên sách
    List<Book> findByBookName(String bookName);

    // Tìm tất cả sách theo thể loại
    List<Book> findByCategory(Category category);

    // Lấy danh sách sách theo năm phát hành
    List<Book> findByPublishYear(int publishYear);

    // Lấy ảnh của sách bằng ID (trả về byte[])
    @Query("SELECT b.bookImage FROM Book b WHERE b.bookId = :bookId")
    byte[] findBookImageById(@Param("bookId") Long bookId);
}

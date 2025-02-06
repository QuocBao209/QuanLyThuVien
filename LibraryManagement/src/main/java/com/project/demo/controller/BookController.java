package com.project.demo.controller;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.AuthorService;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;

    @PostMapping("/submit-book-info")
    public String submitBookInfo(@RequestParam("author") String authorName,
                                 @RequestParam("book-title") String bookTitle,
                                 @RequestParam("quantity") int quantity,
                                 @RequestParam("category") String categoryName,
                                 @RequestParam("release-year") int releaseYear) {

        // Kiểm tra và lấy Author từ database
        Author author = authorService.getAuthors().stream()
                .filter(a -> a.getAuthorName().equalsIgnoreCase(authorName))
                .findFirst()
                .orElse(null);

        if (author == null) {
            // Nếu không tìm thấy tác giả, tạo mới Author và lưu vào database
            author = new Author();
            author.setAuthorName(authorName);
            authorService.transferData(List.of(author));  // Lưu tác giả mới vào database
        }

        // Kiểm tra và lấy Category từ database
        Category category = categoryService.getCategories().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(null);

        if (category == null) {
            // Nếu không tìm thấy thể loại, tạo mới Category và lưu vào database
            category = new Category();
            category.setCategoryName(categoryName);
            categoryService.transferData(List.of(category));  // Lưu thể loại mới vào database
        }

        // Tạo đối tượng Book từ các thông tin nhập vào
        Book book = new Book();
        book.setBookName(bookTitle);
        book.setAmount(quantity);
        book.setPublishYear(releaseYear);
        book.setAuthor(author);
        book.setCategory(category);

        // Lưu vào cơ sở dữ liệu
        bookService.transferData(List.of(book));

        // Sau khi lưu thành công, chuyển hướng đến trang khác hoặc gửi thông báo
        return "redirect:/success"; // Trang thành công (có thể thay bằng trang khác)
    }
}

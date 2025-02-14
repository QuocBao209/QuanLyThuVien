package com.project.demo.controller;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.AuthorService;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;

    // Hiển thị form thêm sách
    @GetMapping("/add-book")
    public ModelAndView getAddBookForm() {
        return new ModelAndView("addBook");
    }

    // Xử lý thêm sách (tạm thời chỉ in ra console)
    @PostMapping("/add-book")
    public ModelAndView addBook(@RequestParam("title") String title,
                                @RequestParam("author") String author,
                                @RequestParam("quantity") int amount,
                                @RequestParam("type") String typeName,
                                @RequestParam("release-year") int publishYear) {
        System.out.println(title);
        System.out.println(author);
        System.out.println(amount);
        System.out.println(typeName);
        System.out.println(publishYear);

        return new ModelAndView("addBook");
    }

    // Xử lý lưu sách vào database
    @PostMapping("/submit-book-info")
    public ModelAndView submitBookInfo(@RequestParam("author") String authorName,
                                       @RequestParam("book-title") String bookName,
                                       @RequestParam("quantity") int amount,
                                       @RequestParam("category") String categoryName,
                                       @RequestParam("release-year") int publishYear,
                                       @RequestParam("book-image") MultipartFile bookImage) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            // Kiểm tra hoặc thêm mới Author
            Optional<Author> optionalAuthor = Optional.ofNullable(authorService.findByName(authorName));
            Author author = optionalAuthor.orElseGet(() -> {
                Author newAuthor = new Author();
                newAuthor.setAuthorName(authorName);
                return authorService.saveAuthor(newAuthor);
            });

            // Kiểm tra hoặc thêm mới Category
            Optional<Category> optionalCategory = Optional.ofNullable(categoryService.findByName(categoryName));
            Category category = optionalCategory.orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setCategoryName(categoryName);
                return categoryService.saveCategory(newCategory);
            });

            // Kiểm tra nếu sách đã tồn tại
            Optional<Book> existingBook = bookService.findByBookNameAndAuthor(bookName, author);
            if (existingBook.isPresent()) {
                modelAndView.addObject("message", "Sách đã tồn tại!");
                modelAndView.setViewName("error");
                return modelAndView;
            }

            // Tạo và lưu mới sách
            Book book = new Book();
            book.setBookName(bookName);
            book.setAmount(amount);
            book.setPublishYear(publishYear);
            book.setAuthor(author);
            book.setCategory(category);

            // Xử lý ảnh sách
            if (!bookImage.isEmpty()) {
                book.setBookImage(bookImage.getBytes()); // Lưu ảnh vào database
            }

            bookService.saveBook(book);

            modelAndView.addObject("message", "Thêm sách thành công!");
            modelAndView.setViewName("success");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi xử lý ảnh!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

    // Hiển thị danh sách sách từ database
    @GetMapping("/book-list")
    public ModelAndView showBookListForm() {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks()); // Lấy danh sách sách từ database
        return modelAndView;
    }
}

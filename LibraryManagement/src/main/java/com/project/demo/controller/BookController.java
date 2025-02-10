package com.project.demo.controller;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.AuthorService;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;

import ch.qos.logback.core.model.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;
    
    @GetMapping("/add-book")
    public ModelAndView getAddBookForm() {
        ModelAndView modelAndView = new ModelAndView("addBook");
        return modelAndView;
    }

    @PostMapping("/submit-book-info")
    public ModelAndView submitBookInfo(@RequestParam("author") String authorName,
                                       @RequestParam("book-title") String bookName,
                                       @RequestParam("quantity") int amount,
                                       @RequestParam("category") String categoryName,
                                       @RequestParam("release-year") int publishYear) {
        ModelAndView modelAndView = new ModelAndView();

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
            modelAndView.setViewName("error"); //chưa làm có gì làm dùm
            return modelAndView;
        }

        // Tạo và lưu mới sách
        Book book = new Book();
        book.setBookName(bookName);
        book.setAmount(amount);
        book.setPublishYear(publishYear);
        book.setAuthor(author);
        book.setCategory(category);
        bookService.saveBook(book);

        modelAndView.addObject("message", "Thêm sách thành công!");
        modelAndView.setViewName("success"); // chưa làm nốt làm lun đi m
        return modelAndView;
    }
}

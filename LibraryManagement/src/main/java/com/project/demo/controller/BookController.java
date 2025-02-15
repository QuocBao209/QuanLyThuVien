package com.project.demo.controller;

import com.project.demo.entity.Author;
import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.AuthorService;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @GetMapping("/add-book")
    public ModelAndView getAddBookForm() {
        return new ModelAndView("addBook");
    }

    @PostMapping("/submit-book-info")
    public ModelAndView submitBookInfo(@RequestParam("author") String authorName,
                                       @RequestParam("book-title") String bookName,
                                       @RequestParam("amount") int amount,
                                       @RequestParam("category") String categoryName,
                                       @RequestParam("publish-year") int publishYear,
                                       @RequestParam("book-image") MultipartFile bookImage) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            if (!bookImage.isEmpty()) {
                if (!ALLOWED_IMAGE_TYPES.contains(bookImage.getContentType())) {
                    modelAndView.addObject("message", "Chỉ chấp nhận ảnh JPG, PNG hoặc GIF!");
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
                if (bookImage.getSize() > MAX_IMAGE_SIZE) {
                    modelAndView.addObject("message", "Ảnh quá lớn! Kích thước tối đa là 5MB.");
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
            }

            Optional<Author> optionalAuthor = Optional.ofNullable(authorService.findByName(authorName));
            Author author = optionalAuthor.orElseGet(() -> {
                Author newAuthor = new Author();
                newAuthor.setAuthorName(authorName);
                return authorService.saveAuthor(newAuthor);
            });

            Optional<Category> optionalCategory = Optional.ofNullable(categoryService.findByName(categoryName));
            Category category = optionalCategory.orElseGet(() -> {
                Category newCategory = new Category();
                newCategory.setCategoryName(categoryName);
                return categoryService.saveCategory(newCategory);
            });

            Optional<Book> existingBook = bookService.findByBookNameAndAuthor(bookName, author);
            if (existingBook.isPresent()) {
                modelAndView.addObject("message", "Sách đã tồn tại!");
                modelAndView.setViewName("error");
                return modelAndView;
            }

            Book book = new Book();
            book.setBookName(bookName);
            book.setAmount(amount);
            book.setPublishYear(publishYear);
            book.setAuthor(author);
            book.setCategory(category);

            if (!bookImage.isEmpty()) {
                book.setBookImage(bookImage.getBytes());
            }

            bookService.saveBook(book);
            modelAndView.addObject("message", "Thêm sách thành công!");
            modelAndView.setViewName("bookList");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi xử lý ảnh!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

    @GetMapping("/book-list")
    public ModelAndView showBookListForm() {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks());
        return modelAndView;
    }

    @GetMapping("/edit-book/{id}")
    public ModelAndView showBookEditForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("bookEdit");
        Book book = bookService.getBookById(id);
        modelAndView.addObject("categories", categoryService.getCategories());
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    @PostMapping("/update-book/{id}")
    public ModelAndView updateBook(@PathVariable Long id,
                                   @RequestParam("book-title") String bookName,
                                   @RequestParam("amount") int amount,
                                   @RequestParam("category") String categoryName,
                                   @RequestParam("publish-year") int publishYear,
                                   @RequestParam("book-image") MultipartFile bookImage) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                modelAndView.addObject("message", "Không tìm thấy sách!");
                modelAndView.setViewName("error");
                return modelAndView;
            }
            book.setBookName(bookName);
            book.setAmount(amount);
            book.setPublishYear(publishYear);
            book.setCategory(categoryService.findByName(categoryName));
            if (!bookImage.isEmpty()) {
                book.setBookImage(bookImage.getBytes());
            }
            bookService.saveBook(book);
            modelAndView.addObject("message", "Cập nhật sách thành công!");
            modelAndView.setViewName("bookList");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi xử lý ảnh!");
            modelAndView.setViewName("error");
        }
        return modelAndView;
    }

    @GetMapping("/delete-book/{id}")
    public ModelAndView deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return new ModelAndView("redirect:/admin/book-list");
    }
}

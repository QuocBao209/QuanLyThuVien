package com.project.admin.controller;

import com.project.admin.entity.Author;
import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import com.project.admin.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;
    @Autowired private ExcelBookService excelBookService;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String IMAGE_UPLOAD_DIR = "uploads/book_images/";

    // Hiển thị trang import sách
    @PostMapping("/import-book")
    public String importBookForm() {
        return "importBook";
    }
    // Hiển thị lựa chọn thêm sách
    @PostMapping("/add-book-option")
    public String addBookOption() {
        return "addBookOption";
    }

    // Hiển thị form thêm sách
    @PostMapping("/add-book")
    public ModelAndView getAddBookForm() {
        ModelAndView modelAndView = new ModelAndView("addBook");
        modelAndView.addObject("categories", categoryService.getCategories());
        modelAndView.addObject("authors", authorService.getAuthors());
        return modelAndView;
    }

    // Xử lý lưu sách vào database
    @PostMapping("/submit-book-info")
    public ModelAndView submitBookInfo(@RequestParam(value="book-id", required = false) Long bookId,
                                       @RequestParam("authors") List<String> authorNames,
                                       @RequestParam("book-title") String bookName,
                                       @RequestParam("amount") int amount,
                                       @RequestParam("category") String categoryName,
                                       @RequestParam("publish-year") int publishYear,
                                       @RequestParam("book-image") MultipartFile bookImage) {
        ModelAndView modelAndView = new ModelAndView();

        try {
            // Kiểm tra định dạng ảnh
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

            // Xử lý danh sách tác giả
            List<Author> authors = authorNames.stream()
                    .map(name -> authorService.findByName(name)
                            .orElseGet(() -> authorService.saveAuthor(new Author(name))))
                    .collect(Collectors.toList());

            // Kiểm tra hoặc thêm mới Category
            Category category = categoryService.findByName(categoryName);
            if (category == null) {
                category = new Category();
                category.setCategoryName(categoryName);
                category = categoryService.saveCategory(category);
            }

            Book book;
            if (bookId != null) {
                book = bookService.getBookById(bookId);
                if (book == null) {
                    modelAndView.addObject("message", "Sách không tồn tại!");
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
            } else {
                Optional<Book> existingBook = bookService.findByBookNameAndAuthors(bookName, authors);
                if (existingBook.isPresent()) {
                    modelAndView.addObject("message", "Sách đã tồn tại!");
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
                book = new Book();
            }

            // Cập nhật thông tin sách
            book.setBookName(bookName);
            book.setAmount(amount);
            book.setPublishYear(publishYear);
            book.setAuthors(authors);
            book.setCategory(category);

            if (!bookImage.isEmpty()) {
                File uploadDir = new File(IMAGE_UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String fileName = UUID.randomUUID() + "_" + bookImage.getOriginalFilename();
                Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
                Files.write(filePath, bookImage.getBytes());

                // Chỉ lưu tên file vào database
                book.setBookImage(fileName);
            }



            bookService.saveBook(book);
            modelAndView.addObject("message", "Thao tác thành công!");
            modelAndView.setViewName("forward:/admin/book-list");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi xử lý ảnh!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

    // Hiển thị danh sách sách
    @PostMapping("/book-list")
    public ModelAndView showBookListForm() {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks()); 
        return modelAndView;
    }

    // Hiển thị form chỉnh sửa sách
    @PostMapping("/edit-book/{id}")
    public ModelAndView showBookEditForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("bookEdit");
        Book book = bookService.getBookById(id);
        if (book == null) {
            modelAndView.addObject("message", "Không tìm thấy sách!");
            modelAndView.setViewName("error");
            return modelAndView;
        }
        modelAndView.addObject("authors", authorService.getAuthors());
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    // Xóa sách
    @PostMapping("/delete-book/{id}")
    public ModelAndView deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks());
        return modelAndView;
    }
    
    // Xử lý import từ excel
    @PostMapping("/upload-excel")
    public ModelAndView uploadExcel(@RequestParam("file") MultipartFile file) {
        ModelAndView modelAndView = new ModelAndView();
        if (file.isEmpty()) {
            modelAndView.addObject("message", "Vui lòng chọn file Excel!");
            modelAndView.setViewName("error");
            return modelAndView;
        }

        try {
            List<Book> importedBooks = excelBookService.importBooksFromExcel(file);
            modelAndView.addObject("message", "Thêm thành công " + importedBooks.size() + " sách!");
            modelAndView.setViewName("forward:/admin/book-list");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi đọc file Excel!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

}


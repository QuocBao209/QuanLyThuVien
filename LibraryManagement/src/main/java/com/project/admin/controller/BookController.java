package com.project.admin.controller;

import com.project.admin.entity.*;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class BookController {

    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;
    @Autowired private ExcelBookService excelBookService;
    @Autowired private ImportService importService;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String IMAGE_UPLOAD_DIR = "uploads/book_images/";

    // Hiển thị danh sách nhâp hàng
    @PostMapping("/import")
    public String importForm() {
        return "import";
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
            String fileName = "";
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
                fileName = bookImage.getOriginalFilename();
            }

            // Xử lý danh sách tác giả
            List<Author> authors = authorNames.stream()
                    .map(name -> authorService.findByName(name)
                            .orElseGet(() -> authorService.saveAuthor(new Author(name))))
                    .collect(Collectors.toList());

            // Kiểm tra hoặc thêm mới Category
            Category category = categoryService.findByName(categoryName);
            if (category == null) {
                category = categoryService.saveCategory(new Category(categoryName));
            }

            Optional<Book> existingBook = bookService.findExactMatch(bookName, authors, category, publishYear);
            Book book;

            if (existingBook.isPresent()) {
                book = existingBook.get();
                book.setAmount(book.getAmount() + amount);
                if (!fileName.isEmpty() && (book.getBookImage() == null || book.getBookImage().isEmpty())) {
                    saveBookImage(bookImage, fileName);
                    book.setBookImage(fileName);
                }
            } else {
                book = new Book();
                book.setBookName(bookName);
                book.setAmount(amount);
                book.setPublishYear(publishYear);
                book.setAuthors(authors);
                book.setCategory(category);

                if (!fileName.isEmpty()) {
                    saveBookImage(bookImage, fileName);
                    book.setBookImage(fileName);
                }
            }

            bookService.saveBook(book);

            ImportDetail importDetail = new ImportDetail();
            importDetail.setBook(book);
            importDetail.setAmount(amount);
            importService.importBooks(Collections.singletonList(importDetail), LocalDate.now());

            modelAndView.addObject("message", "Thao tác thành công!");
            modelAndView.setViewName("forward:/admin/book-list");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi xử lý ảnh!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }
    
    // Danh sách sách
    @PostMapping("/book-list")
    public ModelAndView showBookListForm() {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks());
        return modelAndView;
    }
    
    // Chỉnh sửa sách
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
        bookService.deleteBook(id); // Không xóa hẳn, chỉ đánh dấu là đã xóa
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks()); // Chỉ lấy sách chưa bị xóa
        return modelAndView;
    }

    // Thêm sách từ Excel
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
            for (Book book : importedBooks) {
                Optional<Book> existingBook = bookService.findExactMatch(
                        book.getBookName(),
                        book.getAuthors(),
                        book.getCategory(),
                        book.getPublishYear()
                );

                if (existingBook.isPresent()) {
                    Book foundBook = existingBook.get();
                    foundBook.setAmount(foundBook.getAmount() + book.getAmount());
                    bookService.saveBook(foundBook);
                } else {
                    bookService.saveBook(book);
                }
            }

            modelAndView.addObject("message", "Thêm thành công " + importedBooks.size() + " sách!");
            modelAndView.setViewName("forward:/admin/book-list");
        } catch (IOException e) {
            modelAndView.addObject("message", "Lỗi khi đọc file Excel!");
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }
    
    // Xử lý lưu ảnh
    private void saveBookImage(MultipartFile bookImage, String fileName) throws IOException {
        File uploadDir = new File(IMAGE_UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
        Files.write(filePath, bookImage.getBytes());
    }
    
    // Tìm kiếm sách
    @PostMapping("/search-book")
    public ModelAndView searchBook(@RequestParam("keyword") String keyword) {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.searchBooks(keyword));
        return modelAndView;
    }
}
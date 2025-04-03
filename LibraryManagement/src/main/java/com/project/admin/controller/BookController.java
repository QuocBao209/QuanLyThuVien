package com.project.admin.controller;

import com.project.admin.entity.*;
import com.project.admin.service.*;
import com.project.admin.utils.AdminCodes;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    
    // Danh sách nhập hàng
    @PostMapping("/import")
    public String showImportList(Model model) {
        List<ImportReceipt> importReceipts = importService.getAllImportReceipts();
        model.addAttribute("importReceipts", importReceipts);
        return "import";
    }
    
    @PostMapping("/import-receipt-detail")
    public String showImportDetailList(Model model) {
        List<ImportDetail> importDetails = importService.getAllImportDetails();
        model.addAttribute("importDetails", importDetails);
        return "importDetails";
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
            int currentYear = java.time.Year.now().getValue();

            // Kiểm tra số lượng sách hợp lệ (chỉ nhận số nguyên dương)
            if (amount <= 0) {
                modelAndView.addObject("message", AdminCodes.getErrorMessage("INVALID_BOOK_AMOUNT"));
                modelAndView.setViewName("error");
                return modelAndView;
            }

            // Kiểm tra năm xuất bản hợp lệ
            if (publishYear <= 1000 || publishYear > currentYear) {
                modelAndView.addObject("message", String.format(AdminCodes.getErrorMessage("INVALID_PUBLISH_YEAR"), currentYear));
                modelAndView.setViewName("error");
                return modelAndView;
            }

            // Kiểm tra và định dạng tên tác giả
            List<String> formattedAuthors = new ArrayList<>();
            for (String author : authorNames) {
                if (!author.matches("^[a-zA-ZÀ-Ỹà-ỹ.,\\s]+$")) {
                    modelAndView.addObject("message", AdminCodes.getErrorMessage("INVALID_AUTHOR_NAME"));
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
                formattedAuthors.add(capitalizeEachWord(author));
            }

            // Kiểm tra và định dạng thể loại
            if (!categoryName.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$")) {
                modelAndView.addObject("message", AdminCodes.getErrorMessage("INVALID_CATEGORY_NAME"));
                modelAndView.setViewName("error");
                return modelAndView;
            }
            categoryName = capitalizeEachWord(categoryName);

            String fileName = "";
            if (!bookImage.isEmpty()) {
                if (!ALLOWED_IMAGE_TYPES.contains(bookImage.getContentType())) {
                    modelAndView.addObject("message", AdminCodes.getErrorMessage("INVALID_IMAGE_TYPE"));
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
                if (bookImage.getSize() > MAX_IMAGE_SIZE) {
                    modelAndView.addObject("message", AdminCodes.getErrorMessage("IMAGE_TOO_LARGE"));
                    modelAndView.setViewName("error");
                    return modelAndView;
                }
                fileName = bookImage.getOriginalFilename();
            }

            // Xử lý danh sách tác giả
            List<Author> authors = formattedAuthors.stream()
                    .map(name -> authorService.findByName(name)
                            .orElseGet(() -> authorService.saveAuthor(new Author(name))))
                    .collect(Collectors.toList());

            // Kiểm tra hoặc thêm mới Category
            Category category = categoryService.findByName(categoryName);
            if (category == null) {
                category = categoryService.saveCategory(new Category(categoryName));
            }

            Book book;

            if (bookId != null) { // TRƯỜNG HỢP CHỈNH SỬA SÁCH
                book = bookService.getBookById(bookId);
                if (book == null) {
                    modelAndView.addObject("message", AdminCodes.getErrorMessage("BOOK_NOT_EXIST"));
                    modelAndView.setViewName("error");
                    return modelAndView;
                }

                book.setBookName(bookName);
                book.setAmount(amount);
                book.setPublishYear(publishYear);
                book.setAuthors(authors);
                book.setCategory(category);

                if (!fileName.isEmpty()) {
                    saveBookImage(bookImage, fileName);
                    book.setBookImage(fileName);
                }
            } else { // TRƯỜNG HỢP THÊM MỚI SÁCH
                Optional<Book> existingBook = bookService.findExactMatch(bookName, authors, category, publishYear);
                if (existingBook.isPresent()) {
                    book = existingBook.get();
                    book.setAmount(book.getAmount() + amount);
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
            }

            bookService.saveBook(book);

            modelAndView.addObject("message", AdminCodes.getSuccessMessage("OPERATION_SUCCESS"));
            modelAndView.setViewName("forward:/admin/book-list");
        } catch (IOException e) {
            modelAndView.addObject("message", AdminCodes.getErrorMessage("IMAGE_PROCESS_ERROR"));
            modelAndView.setViewName("error");
        }

        return modelAndView;
    }

    // Danh sách sách ở Admin
    private String capitalizeEachWord(String str) {
        String[] words = str.trim().split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalizedString.toString().trim();
    }

    @PostMapping("/book-list")
    public ModelAndView showBookListForm() {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks());
        return modelAndView;
    }

    // Chỉnh sửa sách theo id
    @PostMapping("/edit-book/{id}")
    public ModelAndView showBookEditForm(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView("bookEdit");
        Book book = bookService.getBookById(id);
        if (book == null) {
            modelAndView.addObject("message", AdminCodes.getErrorMessage("BOOK_NOT_FOUND"));
            modelAndView.setViewName("error");
            return modelAndView;
        }
        modelAndView.addObject("authors", authorService.getAuthors());
        modelAndView.addObject("book", book);
        return modelAndView;
    }

    // Xóa sách theo id
    @PostMapping("/delete-book/{id}")
    public ModelAndView deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.getBooks());
        return modelAndView;
    }

    // Controller xử lý Excel
    @PostMapping("/import-view")
    public String importView() {
    	return "importView";
    }

    @PostMapping("/upload-excel")
    public ModelAndView uploadExcel(@RequestParam("file") MultipartFile file, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            modelAndView.setViewName("redirect:/admin-login");
            return modelAndView;
        }

        if (file.isEmpty()) {
            modelAndView.setViewName("forward:/admin/book-list?error=File trống!");
            return modelAndView;
        }

        try {
            int importedCount = excelBookService.importBooksFromExcel(file, userId);
            modelAndView.setViewName("forward:/admin/book-list?success=Đã nhập " + importedCount + " sách!");
        } catch (IOException e) {
            modelAndView.setViewName("forward:/admin/book-list?error=Lỗi khi đọc file Excel!");
        } catch (Exception e) {
            modelAndView.setViewName("forward:/admin/book-list?error=" + e.getMessage());
        }

        return modelAndView;
    }


    // Controller xử lý Ảnh
    private void saveBookImage(MultipartFile bookImage, String fileName) throws IOException {
        File uploadDir = new File(IMAGE_UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        Path filePath = Paths.get(IMAGE_UPLOAD_DIR, fileName);
        Files.write(filePath, bookImage.getBytes());
    }
    
    // Controller xử lý tìm sách
    @PostMapping("/search-book")
    public ModelAndView searchBook(@RequestParam("keyword") String keyword) {
        ModelAndView modelAndView = new ModelAndView("bookList");
        modelAndView.addObject("books", bookService.searchBooks(keyword));
        return modelAndView;
    }
}
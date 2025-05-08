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
    @Autowired private Borrow_ReturnService borrowReturnService;

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
    public String showImportDetailList(@RequestParam String invoiceId,
                                       @RequestParam String importDate,
                                       @RequestParam String userName,
                                       Model model) {
        // Lấy danh sách chi tiết nhập hàng theo invoiceId
        List<ImportDetail> importDetails = importService.getImportDetailsByInvoiceId(invoiceId);

        // Gán thông tin để hiển thị trong view
        model.addAttribute("invoiceId", invoiceId);
        model.addAttribute("importDate", importDate);
        model.addAttribute("userName", userName);
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
        ModelAndView modelAndView = new ModelAndView("addBook");
        
        modelAndView.addObject("categories", categoryService.getCategories());
        modelAndView.addObject("authors", authorService.getAuthors());

        try {
            int currentYear = java.time.Year.now().getValue();

            // Kiểm tra số lượng sách hợp lệ (chỉ nhận số nguyên dương)
            if (amount <= 0) {
                modelAndView.addObject("errorAmount", AdminCodes.getErrorMessage("INVALID_BOOK_AMOUNT"));
                return modelAndView;
            }


            // Kiểm tra năm xuất bản hợp lệ
            if (publishYear <= 1000 || publishYear > currentYear) {
                modelAndView.addObject("errorYear", String.format(AdminCodes.getErrorMessage("INVALID_PUBLISH_YEAR"), currentYear));
                return modelAndView;
            }

            // Kiểm tra và định dạng tên tác giả
            List<String> formattedAuthors = new ArrayList<>();
            for (String author : authorNames) {
                if (!author.matches("^[a-zA-ZÀ-Ỹà-ỹ.,\\s]+$")) {
                    modelAndView.addObject("errorAuthors", AdminCodes.getErrorMessage("INVALID_AUTHOR_NAME"));
                    return modelAndView;
                }
                formattedAuthors.add(capitalizeEachWord(author));
            }

            // Kiểm tra và định dạng thể loại
            if (!categoryName.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$")) {
                modelAndView.addObject("errorCategory", AdminCodes.getErrorMessage("INVALID_CATEGORY_NAME"));
                return modelAndView;
            }
            categoryName = capitalizeEachWord(categoryName);

            String fileName = "";
            if (!bookImage.isEmpty()) {
                if (!ALLOWED_IMAGE_TYPES.contains(bookImage.getContentType())) {
                    modelAndView.addObject("errorImageType", AdminCodes.getErrorMessage("INVALID_IMAGE_TYPE"));
                    return modelAndView;
                }
                if (bookImage.getSize() > MAX_IMAGE_SIZE) {
                    modelAndView.addObject("errorImageSize", AdminCodes.getErrorMessage("IMAGE_TOO_LARGE"));
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

            if (bookId != null) {
                book = bookService.getBookById(bookId);
                if (book == null) {
                    modelAndView.addObject("notExistedBook", AdminCodes.getErrorMessage("BOOK_NOT_EXIST"));
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
            } else {
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

            modelAndView.addObject("successMessage", AdminCodes.getSuccessMessage("OPERATION_SUCCESS"));
            modelAndView.setViewName("forward:/admin/add-book");
        } catch (IOException e) {
            modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("IMAGE_PROCESS_ERROR"));
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
    public ModelAndView editBook(@PathVariable Long id,
                                 @RequestParam(value = "authors", required = false) List<String> authorNames,
                                 @RequestParam(value = "book-title", required = false) String bookName,
                                 @RequestParam(value = "amount", required = false) Integer amount,
                                 @RequestParam(value = "category", required = false) String categoryName,
                                 @RequestParam(value = "publish-year", required = false) Integer publishYear,
                                 @RequestParam(value = "book-image", required = false) MultipartFile bookImage) {

        ModelAndView modelAndView = new ModelAndView("bookEdit");
        modelAndView.addObject("authors", authorService.getAuthors());
        modelAndView.addObject("categories", categoryService.getCategories());

        // Lấy thông tin sách
        Book book = bookService.getBookById(id);
        if (book == null) {
            modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("BOOK_NOT_FOUND"));
            return modelAndView;
        }

        // Nếu chỉ vào trang chỉnh sửa mà chưa submit form
        if (bookName == null) {
            modelAndView.addObject("book", book);
            return modelAndView;
        }

        try {
            int currentYear = java.time.Year.now().getValue();

            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException(AdminCodes.getErrorMessage("INVALID_BOOK_AMOUNT"));
            }

            if (publishYear == null || publishYear <= 1000 || publishYear > currentYear) {
                throw new IllegalArgumentException(String.format(AdminCodes.getErrorMessage("INVALID_PUBLISH_YEAR"), currentYear));
            }

            if (authorNames == null || authorNames.isEmpty()) {
                throw new IllegalArgumentException(AdminCodes.getErrorMessage("INVALID_AUTHOR_NAME"));
            }

            List<String> formattedAuthors = new ArrayList<>();
            for (String author : authorNames) {
                if (!author.matches("^[a-zA-ZÀ-Ỹà-ỹ.,\\s]+$")) {
                    throw new IllegalArgumentException(AdminCodes.getErrorMessage("INVALID_AUTHOR_NAME"));
                }
                formattedAuthors.add(capitalizeEachWord(author));
            }

            if (categoryName == null || !categoryName.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$")) {
                throw new IllegalArgumentException(AdminCodes.getErrorMessage("INVALID_CATEGORY_NAME"));
            }
            categoryName = capitalizeEachWord(categoryName);

            // Kiểm tra ảnh (nếu có)
            String fileName = book.getBookImage();
            if (bookImage != null && !bookImage.isEmpty()) {
                if (!ALLOWED_IMAGE_TYPES.contains(bookImage.getContentType())) {
                    throw new IllegalArgumentException(AdminCodes.getErrorMessage("INVALID_IMAGE_TYPE"));
                }
                if (bookImage.getSize() > MAX_IMAGE_SIZE) {
                    throw new IllegalArgumentException(AdminCodes.getErrorMessage("IMAGE_TOO_LARGE"));
                }
                fileName = bookImage.getOriginalFilename();
                saveBookImage(bookImage, fileName);
            }

            // Cập nhật sách
            book.setBookName(bookName);
            book.setAmount(amount);
            book.setPublishYear(publishYear);
            book.setBookImage(fileName);

            // Cập nhật tác giả
            List<Author> authors = formattedAuthors.stream()
                    .map(name -> authorService.findByName(name)
                            .orElseGet(() -> authorService.saveAuthor(new Author(name))))
                    .collect(Collectors.toList());
            book.setAuthors(authors);

            // Cập nhật thể loại
            Category category = categoryService.findByName(categoryName);
            if (category == null) {
                category = categoryService.saveCategory(new Category(categoryName));
            }
            book.setCategory(category);

            bookService.saveBook(book);
            modelAndView.addObject("successMessage", AdminCodes.getSuccessMessage("BOOK_UPDATED_SUCCESS"));

        } catch (IllegalArgumentException e) {
            modelAndView.addObject("errorMessage", e.getMessage());
        } catch (IOException e) {
            modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("IMAGE_PROCESS_ERROR"));
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", "Lỗi không xác định: " + e.getMessage());
        }

        modelAndView.addObject("book", bookService.getBookById(id));
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
    public ModelAndView searchBook(@RequestParam("keyword") String keyword,
    							   @RequestParam("targetView") String targetView) {
        ModelAndView modelAndView = new ModelAndView(targetView);
        
        if ("borrow_return_view".equals(targetView)) {
            List<Borrow_Return> results = borrowReturnService.searchByBookOrAuthor(keyword);
            modelAndView.addObject("borrowReturns", results);
            
            if (results.isEmpty()) {
            	modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("BOOK_NOT_FOUND"));
            }
            
        } else {
            List<Book> books = bookService.searchBooks(keyword);
            modelAndView.addObject("books", books);
            
            if (books.isEmpty()) {
            	modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("BOOK_NOT_FOUND"));
            }
        }
        
        return modelAndView;
    }
}
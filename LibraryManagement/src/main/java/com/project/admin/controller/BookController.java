package com.project.admin.controller;

import com.project.admin.entity.*;
import com.project.admin.service.*;
import com.project.admin.utils.AdminCodes;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    @Value("${file.export.directory:/tmp/exports}")
    private String exportDirectory;
    
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
                                   @RequestParam("targetView") String targetView,
                                   @RequestParam(value = "importDate", required = false) String importDate) {
        ModelAndView modelAndView = new ModelAndView(targetView);
        
        if ("borrow_return_view".equals(targetView)) {
            List<Borrow_Return> results = borrowReturnService.searchByBookOrAuthor(keyword);
            modelAndView.addObject("borrowReturns", results);
            
            if (results.isEmpty()) {
                modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("BOOK_NOT_FOUND"));
            }
            
        } else if ("importDetails".equals(targetView)) {
            try {
                List<ImportDetail> importDetails;
                if (importDate != null && !importDate.trim().isEmpty()) {
                    // Chuyển đổi importDate từ String (dd/MM/yyyy) sang LocalDate
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate parsedDate = LocalDate.parse(importDate, formatter);
                    // Tìm kiếm ImportDetail theo keyword và importDate
                    importDetails = importService.searchByBookOrAuthorAndDate(keyword, parsedDate);
                } else {
                    // Nếu không có importDate, trả về danh sách rỗng và thông báo lỗi
                    importDetails = Collections.emptyList();
                    modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("MISSING_IMPORT_DATE"));
                }
                
                modelAndView.addObject("importDetails", importDetails);
                modelAndView.addObject("importDate", importDate);
                
                if (importDetails.isEmpty() && (keyword != null && !keyword.trim().isEmpty())) {
                    modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("BOOK_NOT_FOUND"));
                }
                
            } catch (DateTimeParseException e) {
                modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("INVALID_DATE_FORMAT"));
                modelAndView.addObject("importDetails", Collections.emptyList());
                modelAndView.addObject("importDate", importDate);
            }
            
        } else if ("import".equals(targetView)) {
            try {
                List<ImportReceipt> importReceipts;
                
                if ((keyword == null || keyword.trim().isEmpty()) && (importDate == null || importDate.trim().isEmpty())) {
                    // Nếu không có keyword và importDate, trả về toàn bộ danh sách
                    importReceipts = importService.getAllImportReceipts();
                } else if (importDate != null && !importDate.trim().isEmpty()) {
                    // Chuyển đổi importDate từ String (yyyy-MM-dd) sang LocalDate
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate parsedDate = LocalDate.parse(importDate, formatter);
                    // Tìm kiếm theo keyword (nếu có) và importDate
                    importReceipts = importService.searchImportReceipts(keyword, parsedDate);
                } else {
                    // Chỉ tìm kiếm theo keyword
                    importReceipts = importService.searchImportReceipts(keyword, null);
                }
                
                modelAndView.addObject("importReceipts", importReceipts);
                modelAndView.addObject("keyword", keyword);
                modelAndView.addObject("importDate", importDate);
                
                if (importReceipts.isEmpty()) {
                    modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("IMPORT_RECEIPT_NOT_FOUND"));
                }
                
            } catch (DateTimeParseException e) {
                modelAndView.addObject("errorMessage", AdminCodes.getErrorMessage("INVALID_DATE_FORMAT"));
                modelAndView.addObject("importReceipts", Collections.emptyList());
                modelAndView.addObject("keyword", keyword);
                modelAndView.addObject("importDate", importDate);
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

//    @GetMapping("/statistics/export-excel")
//    public String exportBooksToExcel(
//            @RequestParam(required = false) String query,
//            @RequestParam(required = false) Integer fromMonth,
//            @RequestParam(required = false) Integer toMonth,
//            @RequestParam(required = false) Integer fromYear,
//            @RequestParam(required = false) Integer toYear,
//            @RequestParam(required = false) String categoryId,
//            @RequestParam(required = false, defaultValue = "book-borrow-stats") String statisticType,
//            @RequestParam List<String> columns,
//            Model model) throws IOException {
//        // Set default values for null parameters
//        query = query != null ? query : "";
//        fromMonth = fromMonth != null ? fromMonth : 1;
//        toMonth = toMonth != null ? toMonth : 12;
//        int currentYear = LocalDate.now().getYear();
//        fromYear = fromYear != null ? fromYear : 1900;
//        toYear = toYear != null ? toYear : currentYear;
//        categoryId = categoryId != null ? categoryId : "";
//
//        // Validate input parameters
//        if (fromMonth < 1 || fromMonth > 12 || toMonth < 1 || toMonth > 12) {
//            model.addAttribute("errorMessage", "Tháng không hợp lệ (phải từ 1 đến 12)");
//            return "export-error";
//        }
//        if (fromYear < 1900 || fromYear > currentYear || toYear < 1900 || toYear > currentYear) {
//            model.addAttribute("errorMessage", "Năm không hợp lệ (phải từ 1900 đến " + currentYear + ")");
//            return "export-error";
//        }
//        if (fromYear > toYear || (fromYear == toYear && fromMonth > toMonth)) {
//            model.addAttribute("errorMessage", "Khoảng thời gian không hợp lệ: Ngày bắt đầu phải trước ngày kết thúc");
//            return "export-error";
//        }
//        if (columns == null || columns.isEmpty()) {
//            model.addAttribute("errorMessage", "Danh sách cột không được để trống");
//            return "export-error";
//        }
//
//        // Fetch data
//        List<Book> books = bookService.getBooksForStatistics(query, fromMonth, fromYear, toMonth, toYear, categoryId, statisticType);
//
//        // Create unique filename
//        String filename = "thong_ke_sach_" + System.currentTimeMillis() + ".xlsx";
//        String filePath = Paths.get(exportDirectory, filename).toString();
//
//        // Ensure directory exists
//        File directory = new File(exportDirectory);
//        if (!directory.exists() && !directory.mkdirs()) {
//            model.addAttribute("errorMessage", "Không thể tạo thư mục lưu file: " + exportDirectory);
//            return "export-error";
//        }
//
//        // Create workbook
//        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
//            SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("Books Statistics");
//            // Enable column tracking for auto-sizing
//            sheet.trackAllColumnsForAutoSizing();
//
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            int colIndex = 0;
//            for (String column : columns) {
//                switch (column) {
//                    case "Thể loại" -> headerRow.createCell(colIndex++).setCellValue("Thể loại");
//                    case "ID" -> headerRow.createCell(colIndex++).setCellValue("ID");
//                    case "Ảnh" -> headerRow.createCell(colIndex++).setCellValue("Ảnh");
//                    case "Tên sách" -> headerRow.createCell(colIndex++).setCellValue("Tên sách");
//                    case "Tác giả" -> headerRow.createCell(colIndex++).setCellValue("Tác giả");
//                    case "Tổng số sách" -> headerRow.createCell(colIndex++).setCellValue("Tổng số sách");
//                    case "Số sách đang mượn" -> headerRow.createCell(colIndex++).setCellValue("Số sách đang mượn");
//                    case "Số sách sẵn sàng" -> headerRow.createCell(colIndex++).setCellValue("Số sách sẵn sàng");
//                    case "Số sách bị hư hại" -> headerRow.createCell(colIndex++).setCellValue("Số sách bị hư hại");
//                    default -> {
//                        model.addAttribute("errorMessage", "Cột không hợp lệ: " + column);
//                        return "export-error";
//                    }
//                }
//            }
//
//            // Write data rows
//            int rowNum = 1;
//            for (Book book : books) {
//                Row row = sheet.createRow(rowNum++);
//                colIndex = 0;
//
//                if (columns.contains("Thể loại")) row.createCell(colIndex++).setCellValue(book.getCategory().getCategoryName());
//                if (columns.contains("ID")) row.createCell(colIndex++).setCellValue(book.getBookId());
//                if (columns.contains("Ảnh")) row.createCell(colIndex++).setCellValue(book.getBookImage());
//                if (columns.contains("Tên sách")) row.createCell(colIndex++).setCellValue(book.getBookName());
//                if (columns.contains("Tác giả")) {
//                    String authors = String.join(", ", book.getAuthors().stream().map(Author::getAuthorName).toList());
//                    row.createCell(colIndex++).setCellValue(authors);
//                }
//                if (columns.contains("Tổng số sách")) row.createCell(colIndex++).setCellValue((double) book.getAmount());
//                if (columns.contains("Số sách đang mượn")) row.createCell(colIndex++).setCellValue((double) book.getBorrowCount());
//                if (columns.contains("Số sách sẵn sàng")) {
//                    int available = book.getAmount() - book.getBorrowCount() - book.getIsDamaged();
//                    row.createCell(colIndex++).setCellValue((double) available);
//                }
//                if (columns.contains("Số sách bị hư hại")) row.createCell(colIndex++).setCellValue((double) book.getIsDamaged());
//            }
//
//            // Auto-size columns before writing to file
//            for (int i = 0; i < columns.size(); i++) {
//                try {
//                    sheet.autoSizeColumn(i);
//                } catch (IllegalStateException e) {
//                    // Log the error and continue
//                    System.err.println("Failed to auto-size column " + i + ": " + e.getMessage());
//                }
//            }
//
//            // Save file
//            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
//                workbook.write(fileOut);
//                fileOut.flush();
//            }
//        }
//
//       // Pass data to view
//       model.addAttribute("message", "Export thành công!");
//       model.addAttribute("downloadUrl", "/admin/statistics/downloads/" + filename);
//       return "export-success";
//    }
    
    
    @GetMapping("/statistics/downloads/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        if (!filename.matches("[a-zA-Z0-9_\\-\\.]+")) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
        String filePath = Paths.get(exportDirectory, filename).toString();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File không tồn tại");
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .contentLength(resource.contentLength())
                .body(resource);
    }

}
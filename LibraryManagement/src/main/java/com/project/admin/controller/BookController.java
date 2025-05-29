package com.project.admin.controller;

import com.project.admin.entity.*;
import com.project.admin.service.*;
import com.project.admin.utils.AdminCodes;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
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
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate parsedDate = LocalDate.parse(importDate, formatter);
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

                    importReceipts = importService.getAllImportReceipts();
                } else if (importDate != null && !importDate.trim().isEmpty()) {

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate parsedDate = LocalDate.parse(importDate, formatter);

                    importReceipts = importService.searchImportReceipts(keyword, parsedDate);
                } else {

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

    @GetMapping("/statistics/export-excel")
    public ResponseEntity<Resource> exportBooksToExcel(
            @RequestParam List<String> columns,
            @RequestParam List<String> rows,
            @RequestParam(defaultValue = "book-borrow-stats") String statisticType,
            @RequestParam String pageTitle
    ) throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Books");

        // Tạo hàng tiêu đề
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i));
        }

        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        for (int i = 0; i < rows.size(); i++) {
            String[] rowValues = rows.get(i).split("\\|");
            XSSFRow row = sheet.createRow(i + 1);
            row.setHeightInPoints(60);

            for (int j = 0; j < rowValues.length; j++) {
                String cellValue = rowValues[j];
                if (j == 2) {
                    if (cellValue != null && !cellValue.isEmpty()) {
                        String fileName = Paths.get(cellValue).getFileName().toString();
                        String lowerCaseFileName = fileName.toLowerCase();

                        if (lowerCaseFileName.endsWith(".png") || lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")) {
                            File imageFile = new File("uploads\\book_images\\" + fileName);

                            if (imageFile.exists()) {
                                try (InputStream inputStream = new FileInputStream(imageFile)) {
                                    byte[] imageBytes = IOUtils.toByteArray(inputStream);

                                    int pictureType = lowerCaseFileName.endsWith(".png") ? Workbook.PICTURE_TYPE_PNG : Workbook.PICTURE_TYPE_JPEG;
                                    int pictureIdx = workbook.addPicture(imageBytes, pictureType);

                                    XSSFClientAnchor anchor = new XSSFClientAnchor();
                                    anchor.setCol1(j);
                                    anchor.setRow1(i + 1);
                                    anchor.setCol2(j + 1);
                                    anchor.setRow2(i + 2);

                                    XSSFPicture picture = drawing.createPicture(anchor, pictureIdx);
                                    picture.resize(1.0);

                                    row.setHeightInPoints(80);
                                    sheet.setColumnWidth(j, 20 * 256);
                                }
                            }
                        }
                    }
                } else {
                    row.createCell(j).setCellValue(cellValue);
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

        String processedTitle = pageTitle.replace("đ", "d").replace("Đ", "D");

        String normalizedTitle = java.text.Normalizer.normalize(processedTitle, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");

        String currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));


        String fileName = normalizedTitle + "-" + currentDate + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
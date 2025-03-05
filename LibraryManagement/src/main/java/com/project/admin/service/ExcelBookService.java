package com.project.admin.service;

import com.project.admin.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExcelBookService {

    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AuthorService authorService;
    @Autowired
    private ImportService importService;

    public int importBooksFromExcel(MultipartFile file) throws IOException {
        List<Book> bookList = new ArrayList<>();
        List<ImportDetail> importDetails = new ArrayList<>();
        LocalDate importDate = LocalDate.now();

        // Cache để tránh truy vấn CSDL nhiều lần
        Map<String, Category> categoryCache = new HashMap<>();
        Map<String, Author> authorCache = new HashMap<>();
        Map<String, Book> bookCache = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next(); // Bỏ qua tiêu đề

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String bookName = getStringValue(row.getCell(0));
                int amount = getIntValue(row.getCell(1));
                int publishYear = getIntValue(row.getCell(2));
                String categoryName = getStringValue(row.getCell(3));
                String authorNames = getStringValue(row.getCell(4));
                String imageFileName = getStringValue(row.getCell(5));

                if (bookName.isEmpty() || categoryName.isEmpty() || authorNames.isEmpty()) {
                    continue; // Bỏ qua dòng thiếu thông tin quan trọng
                }

                // Lấy hoặc tạo mới danh mục
                Category category = categoryCache.computeIfAbsent(categoryName, k -> {
                    Category foundCategory = categoryService.findByName(k);
                    return foundCategory != null ? foundCategory : categoryService.saveCategory(new Category(k));
                });

                // Lấy danh sách tác giả
                List<Author> authors = new ArrayList<>();
                for (String authorName : authorNames.split(",")) {
                    authorName = authorName.trim();
                    Author author = authorCache.computeIfAbsent(authorName, k ->
                            authorService.findByName(k).orElseGet(() -> authorService.saveAuthor(new Author(k)))
                    );
                    authors.add(author);
                }

                // Kiểm tra sách đã tồn tại trong database
                String bookKey = bookName.toLowerCase() + "-" + publishYear + "-" + categoryName + "-" + String.join(",", authorNames);
                Book book = bookCache.get(bookKey);
                if (book == null) {
                    Optional<Book> existingBook = bookService.findByBookNameAndAuthors(bookName, authors, category, publishYear);
                    if (existingBook.isPresent()) {
                        book = existingBook.get();
                        book.setAmount(book.getAmount() + amount);
                    } else {
                        book = new Book();
                        book.setBookName(bookName);
                        book.setAmount(amount);
                        book.setPublishYear(publishYear);
                        book.setCategory(category);
                        book.setAuthors(authors);

                        if (!imageFileName.isEmpty()) {
                            book.setBookImage(imageFileName);
                        }
                        bookList.add(book);
                    }
                    bookCache.put(bookKey, book);
                } else {
                    book.setAmount(book.getAmount() + amount);
                }

                // Thêm thông tin nhập kho
                ImportDetail importDetail = new ImportDetail();
                importDetail.setBook(book);
                importDetail.setAmount(amount);
                importDetails.add(importDetail);
            }
        }

        // Lưu sách và thông tin nhập kho
        bookService.transferData(bookList);
        importService.importBooks(importDetails, importDate);

        return bookList.size();
    }


    // Hàm hỗ trợ đọc giá trị từ ô Excel, tránh lỗi null
    private String getStringValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : "";
    }

    private int getIntValue(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return 0;
        return (int) cell.getNumericCellValue();
    }
}

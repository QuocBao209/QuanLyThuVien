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

    public List<Book> importBooksFromExcel(MultipartFile file) throws IOException {
        List<Book> bookList = new ArrayList<>();
        List<ImportDetail> importDetails = new ArrayList<>();
        LocalDate importDate = LocalDate.now();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String bookName = row.getCell(0).getStringCellValue();
                int amount = (int) row.getCell(1).getNumericCellValue();
                int publishYear = (int) row.getCell(2).getNumericCellValue();
                String categoryName = row.getCell(3).getStringCellValue();
                String authorNames = row.getCell(4).getStringCellValue();
                String imageFileName = row.getCell(5) != null ? row.getCell(5).getStringCellValue() : "";

                // Tìm hoặc thêm category
                Category category = categoryService.findByName(categoryName);
                if (category == null) {
                    category = new Category(categoryName);
                    category.setCategoryName(categoryName);
                    category = categoryService.saveCategory(category);
                }

                // Xử lý danh sách tác giả
                List<Author> authors = new ArrayList<>();
                for (String authorName : authorNames.split(",")) {
                    authorName = authorName.trim();
                    String finalAuthorName = authorName;
                    Author author = authorService.findByName(authorName)
                            .orElseGet(() -> authorService.saveAuthor(new Author(finalAuthorName)));
                    authors.add(author);
                }

                // Kiểm tra sách đã tồn tại chưa
                Optional<Book> existingBook = bookService.findByBookNameAndAuthors(bookName, authors);
                Book book;
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

                    // Nếu có ảnh, lưu tên file ảnh vào database
                    if (!imageFileName.isEmpty()) {
                        book.setBookImage(imageFileName);
                    }
                    bookList.add(book);
                }

                // Tạo ImportDetail
                ImportDetail importDetail = new ImportDetail();
                importDetail.setBook(book);
                importDetail.setAmount(amount);
                importDetails.add(importDetail);
            }
        }

        // Lưu sách vào database
        bookService.transferData(bookList);

        // Lưu thông tin nhập kho
        importService.importBooks(importDetails, importDate);

        return bookList;
    }
}

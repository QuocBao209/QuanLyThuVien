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

        Map<String, Category> categoryCache = new HashMap<>();
        Map<String, Author> authorCache = new HashMap<>();
        Map<String, Book> bookCache = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String bookName = getStringValue(row.getCell(0));
                int amount = getIntValue(row.getCell(1));
                int publishYear = getIntValue(row.getCell(2));
                String categoryName = getStringValue(row.getCell(3));
                String authorNames = getStringValue(row.getCell(4));
                String imageFileName = getStringValue(row.getCell(5));

                if (bookName.isEmpty() || categoryName.isEmpty() || authorNames.isEmpty()) {
                    continue;
                }

                Category category = categoryCache.computeIfAbsent(categoryName, k -> {
                    Category foundCategory = categoryService.findByName(k);
                    return foundCategory != null ? foundCategory : categoryService.saveCategory(new Category(k));
                });

                List<Author> authors = new ArrayList<>();
                for (String authorName : authorNames.split(",")) {
                    authorName = authorName.trim();
                    Author author = authorCache.computeIfAbsent(authorName, k ->
                            authorService.findByName(k).orElseGet(() -> authorService.saveAuthor(new Author(k)))
                    );
                    authors.add(author);
                }

                String bookKey = bookName.toLowerCase() + "-" + publishYear + "-" + categoryName + "-" + String.join(",", authorNames);
                Book book = bookCache.get(bookKey);
                if (book == null) {
                    Optional<Book> existingBook = bookService.findByBookNameAndAuthors(bookName, authors, category, publishYear);
                    book = existingBook.orElseGet(() -> {
                        Book newBook = new Book();
                        newBook.setBookName(bookName);
                        newBook.setAmount(0);
                        newBook.setPublishYear(publishYear);
                        newBook.setCategory(category);
                        newBook.setAuthors(authors);
                        if (!imageFileName.isEmpty()) {
                            newBook.setBookImage(imageFileName);
                        }
                        bookList.add(newBook);
                        return newBook;
                    });
                    bookCache.put(bookKey, book);
                }

                ImportDetail importDetail = new ImportDetail();
                importDetail.setBook(book);
                importDetail.setAmount(amount);
                importDetails.add(importDetail);
            }
        }

        bookService.transferData(bookList);
        importService.importBooks(importDetails, importDate);

        return bookList.size();
    }

    private String getStringValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
        return cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : "";
    }

    private int getIntValue(Cell cell) {
        if (cell == null || cell.getCellType() != CellType.NUMERIC) return 0;
        return (int) cell.getNumericCellValue();
    }
}

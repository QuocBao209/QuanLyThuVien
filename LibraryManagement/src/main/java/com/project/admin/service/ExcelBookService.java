package com.project.admin.service;

import com.project.admin.entity.Author;
import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelBookService {

    @Autowired
    private BookService bookService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AuthorService authorService;

    public List<Book> importBooksFromExcel(MultipartFile file) throws IOException {
        List<Book> bookList = new ArrayList<>();

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
                    category = new Category();
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
                if (bookService.findByBookNameAndAuthors(bookName, authors).isEmpty()) {
                    Book book = new Book();
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
            }
        }

        bookService.transferData(bookList);
        return bookList;
    }

}

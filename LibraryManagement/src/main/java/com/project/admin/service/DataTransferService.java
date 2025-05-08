package com.project.admin.service;

import com.project.admin.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class DataTransferService {

    @Autowired private UserService userService;
    @Autowired private BookService bookService;
    @Autowired private AuthorService authorService;
    @Autowired private CategoryService categoryService;
    @Autowired private Borrow_ReturnService borrowReturnService;
    @Autowired private ImportService importService;

    @Transactional
    public void transferData() {
        List<User> users = userService.getUsers();
        List<Book> books = bookService.getBooks();
        List<Author> authors = authorService.getAuthors();
        List<Category> categories = categoryService.getCategories();
        List<Borrow_Return> borrowReturns = borrowReturnService.getBorrowReturns();
        List<ImportReceipt> importReceipts = importService.getAllImportReceipts();
        List<ImportDetail> importDetails = importService.getAllImportDetails();

        userService.transferData(users);
        bookService.transferData(books);
        authorService.transferData(authors);
        categoryService.transferData(categories);
        borrowReturnService.transferData(borrowReturns);
        importService.transferImportReceipts(importReceipts);
        importService.transferImportDetails(importDetails);

    }

}

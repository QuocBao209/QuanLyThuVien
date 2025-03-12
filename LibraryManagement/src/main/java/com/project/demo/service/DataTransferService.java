package com.project.demo.service;

import com.project.demo.entity.*;
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
    @Autowired private NotificationService notificationService;

    @Transactional
    public void transferData() {
        List<User> users = userService.getUsers();
        List<Book> books = bookService.getBooks();
        List<Author> authors = authorService.getAuthors();
        List<Category> categories = categoryService.getCategories();
        List<Borrow_Return> borrowReturns = borrowReturnService.getBorrowReturns();
        List<Notification> notifications = notificationService.getAllNotifications();

        userService.transferData(users);
        bookService.transferData(books);
        authorService.transferData(authors);
        categoryService.transferData(categories);
        borrowReturnService.transferData(borrowReturns);
        notificationService.transferData(notifications);

        System.out.println("Dữ liệu đã được lấy và lưu lại thành công!");
    }
}

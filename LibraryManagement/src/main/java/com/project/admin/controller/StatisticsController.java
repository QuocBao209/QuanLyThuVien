package com.project.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.admin.entity.Book;
import com.project.admin.service.BookService;

@Controller
@RequestMapping("/admin")
public class StatisticsController {

	@Autowired private BookService bookService;
	
	@PostMapping("/statistics/books-per-month")
	public String monthlyBorrowForm() {
		return "monthly_borrow";
	}
	
	// Thống kê sách theo Tháng
    @PostMapping("/statistics/books-per-month/book-borrow-stats")
    public String getBorrowStats(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year,
                                 Model model) {
        List<Book> books = bookService.getBooksByMonthAndYear(query, month, year);
        model.addAttribute("books", books);
        return "monthly_borrow";
    }
}

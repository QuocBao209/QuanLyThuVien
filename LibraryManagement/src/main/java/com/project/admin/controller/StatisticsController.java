package com.project.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.admin.entity.Book;
import com.project.admin.service.BookService;
import com.project.admin.service.UserService;

@Controller
@RequestMapping("/admin")
public class StatisticsController {

	@Autowired private BookService bookService;
	@Autowired private UserService userService;
	@PostMapping("/statistics/books-per-month")
	public String monthlyBorrowForm(Model model) {
	    List<Book> books = bookService.getBooksByMonthAndYear(null, null, null); // Lấy toàn bộ danh sách
	    model.addAttribute("books", books);
	    return "monthly_borrow";
	}
	
	// Thống kê sách theo Tháng
    @PostMapping("/statistics/books-per-month/book-borrow-stats")
    public String getBorrowStats(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year,
                                 Model model) {
        List<Book> books = bookService.getBooksByMonthAndYear(query, month, year);
        
        // Lưu giá trị để hiển thị lại trên giao diện
        model.addAttribute("books", books);
        model.addAttribute("month", month); // Giữ giá trị tháng
        model.addAttribute("year", year);   // Giữ giá trị năm
        
        return "monthly_borrow";
    }
    //Top user mượn nhiều
    @PostMapping("/statistics/top-readers")
    public ModelAndView getTopBorrowers() {
        ModelAndView modelAndView = new ModelAndView("topReader");
        modelAndView.addObject("topReader", userService.getTop3UsersByBorrowCount());
        modelAndView.addObject("reader", userService.getRemainingUsersByBorrowCount());
        return modelAndView;
    }

}

package com.project.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.admin.entity.Book;
import com.project.admin.entity.Category;
import com.project.admin.service.BookBorrowStatsService;
import com.project.admin.service.BookService;
import com.project.admin.service.UserService;
import com.project.admin.service.CategoryService;

@Controller
@RequestMapping("/admin")
public class StatisticsController {

	@Autowired private BookService bookService;
	@Autowired private UserService userService;
	@Autowired private CategoryService categoryService;
	@Autowired private BookBorrowStatsService bookBorrowStatsService;
	
	@PostMapping("/statistics/all")
	public String monthlyBorrowForm(Model model,
									@RequestParam(value = "month", required = false) Integer month,
						            @RequestParam(value = "year", required = false) Integer year) {
        
        // Tổng quan số liệu
        int totalBooks = bookBorrowStatsService.getTotalBooks();
        int totalBorrowing = bookBorrowStatsService.getTotalBorrowing(month, year);
        int totalAvailable = bookBorrowStatsService.getTotalAvailable(month, year);
        int totalDamaged = bookBorrowStatsService.getTotalDamaged();
        
		List<Category> categories = categoryService.getAllCategories();
	    List<Book> books = bookService.getBooksByMonthAndYear(null, null, null, null, null, null); // Lấy toàn bộ danh sách
	    
	    model.addAttribute("books", books);
	    model.addAttribute("categories", categories);
	    model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
	    return "monthly_borrow";
	}
	
	// Thống kê sách theo Tháng - Năm
    @PostMapping("/statistics/books-per-month/book-borrow-stats")
    public String getBorrowStats(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Integer fromMonth,
                                 @RequestParam(required = false) Integer fromYear,
                                 @RequestParam(required = false) Integer toMonth,
                                 @RequestParam(required = false) Integer toYear,
                                 @RequestParam(required = false) Integer categoryId,
								 @RequestParam(value = "month", required = false) Integer month,
						         @RequestParam(value = "year", required = false) Integer year,
                                 Model model) {
    	
    	// Lọc sách theo khoảng thời gian
    	List<Book> books = bookService.getBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
    	List<Category> categories = categoryService.getAllCategories();
    	
    	// Tổng quan số liệu
    	int totalBooks = bookBorrowStatsService.getTotalBooksByCategory(categoryId); // Thống kê tổng số sách theo thể loại
        int totalBorrowing = bookBorrowStatsService.getTotalBorrowingByCategory(categoryId, month, year); // Sách đang mượn theo thể loại
        int totalAvailable = bookBorrowStatsService.getTotalAvailableByCategory(categoryId, month, year); // Sách sẵn sàng theo thể loại
        int totalDamaged = bookBorrowStatsService.getTotalDamagedByCategory(categoryId); // Sách bị hư hỏng theo thể loại
        
        // Lưu dữ liệu vào model để hiển thị lại trên giao diện
        model.addAttribute("books", books);
        model.addAttribute("query", query);
        model.addAttribute("fromMonth", fromMonth);
        model.addAttribute("fromYear", fromYear);
        model.addAttribute("toMonth", toMonth);
        model.addAttribute("toYear", toYear);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId); // Giữ lại lựa chọn thể loại khi submit form
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        
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
    // Thống kê độc giả theo Tháng - Năm
    @PostMapping("/statistics/top-readers/search-reader")
    public ModelAndView getSearchBorrowers(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Integer fromMonth,
                                 @RequestParam(required = false) Integer fromYear,
                                 @RequestParam(required = false) Integer toMonth,
                                 @RequestParam(required = false) Integer toYear) {
    	
    	ModelAndView modelAndView = new ModelAndView("topReader");
        modelAndView.addObject("topReader", userService.getTop3UsersByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear));
        modelAndView.addObject("reader", userService.getRemainingUsersByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear));
        return modelAndView;
    }
    
    // Lọc sách đang mượn
    @PostMapping("/statistics/borrowing")
    public String getBorrowingBook(Model model,
    							   @RequestParam(value = "month", required = false) Integer month,
    							   @RequestParam(value = "year", required = false) Integer year) {
    	
    	// Tổng quan số liệu
        int totalBooks = bookBorrowStatsService.getTotalBooks();
        int totalBorrowing = bookBorrowStatsService.getTotalBorrowing(month, year);
        int totalAvailable = bookBorrowStatsService.getTotalAvailable(month, year);
        int totalDamaged = bookBorrowStatsService.getTotalDamaged();
    
	    List<Category> categories = categoryService.getAllCategories();
	    List<Book> books = bookService.getBorrowingBooksByMonthAndYear(null, null, null, null, null, null); // Lấy toàn bộ danh sách
	    
	    model.addAttribute("books", books);
	    model.addAttribute("categories", categories);
	    model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
	    
	    return "borrowing_report";
    }
}

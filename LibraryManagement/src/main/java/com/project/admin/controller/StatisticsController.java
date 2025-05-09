package com.project.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import com.project.admin.utils.AdminCodes;
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
                                    @RequestParam(value = "year", required = false) Integer year,
                                    @RequestParam(required = false) String selectedBox) {
        
        // Tổng quan số liệu
        int totalBooks = bookBorrowStatsService.getTotalBooks();
        int totalBorrowing = bookBorrowStatsService.getTotalBorrowing(month, year);
        int totalAvailable = bookBorrowStatsService.getTotalAvailable(month, year);
        int totalDamaged = bookBorrowStatsService.getTotalDamaged();
        
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getBooksByMonthAndYear(null, month, year, month, year, null); // Lấy toàn bộ danh sách
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        
        return "monthly_borrow";
    }

    @PostMapping("/statistics/all/book-borrow-stats")
    public String getBorrowStats(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Integer fromMonth,
                                 @RequestParam(required = false) Integer fromYear,
                                 @RequestParam(required = false) Integer toMonth,
                                 @RequestParam(required = false) Integer toYear,
                                 @RequestParam(required = false) Integer categoryId,
                                 @RequestParam(required = false) String selectedBox,
                                 Model model) {
        
        // Kiểm tra tính đầy đủ của Tháng và Năm
        if ((fromMonth != null && fromYear == null) || (toMonth != null && toYear == null)) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_COMPLETENESS"));
            return prepareModelForMonthlyBorrow(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox);
        }

        // Kiểm tra khoảng thời gian hợp lệ
        if (fromYear != null && toYear != null && 
            (fromYear > toYear || (fromYear.equals(toYear) && fromMonth != null && toMonth != null && fromMonth > toMonth))) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_RANGE"));
            return prepareModelForMonthlyBorrow(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox);
        }

        // Lọc sách theo khoảng thời gian
        List<Book> books = bookService.getBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        // Tính toán số liệu tổng quan
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream().mapToLong(Book::getBorrowCount).sum();
        long totalAvailable = books.stream().mapToLong(book -> book.getAmount() - book.getBorrowCount() - book.getIsDamaged()).sum();
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();

        // Lưu dữ liệu vào model
        model.addAttribute("books", books);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("fromMonth", fromMonth);
        model.addAttribute("fromYear", fromYear);
        model.addAttribute("toMonth", toMonth);
        model.addAttribute("toYear", toYear);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        
        return "monthly_borrow";
    }

    @PostMapping("/statistics/borrowing")
    public String getBorrowingBook(Model model,
                                   @RequestParam(value = "month", required = false) Integer month,
                                   @RequestParam(value = "year", required = false) Integer year,
                                   @RequestParam(required = false) String selectedBox) {
        
        // Tổng quan số liệu
        int totalBooks = bookBorrowStatsService.getTotalBooks();
        int totalBorrowing = bookBorrowStatsService.getTotalBorrowing(month, year);
        int totalAvailable = bookBorrowStatsService.getTotalAvailable(month, year);
        int totalDamaged = bookBorrowStatsService.getTotalDamaged();
    
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getBorrowingBooksByMonthAndYear(null, month, year, month, year, null)
                                     .stream()
                                     .filter(book -> book.getBorrowCount() > 0)
                                     .collect(Collectors.toList());
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        
        return "borrowing_report";
    }

    @PostMapping("/statistics/borrowing/book-borrow-stats")
    public String getBorrowingStats(@RequestParam(required = false) String query,
                                    @RequestParam(required = false) Integer fromMonth,
                                    @RequestParam(required = false) Integer fromYear,
                                    @RequestParam(required = false) Integer toMonth,
                                    @RequestParam(required = false) Integer toYear,
                                    @RequestParam(required = false) Integer categoryId,
                                    @RequestParam(required = false) String selectedBox,
                                    Model model) {
        
        // Kiểm tra tính đầy đủ của Tháng và Năm
        if ((fromMonth != null && fromYear == null) || (toMonth != null && toYear == null)) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_COMPLETENESS"));
            return prepareModelForBorrowingReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox);
        }

        // Kiểm tra khoảng thời gian hợp lệ
        if (fromYear != null && toYear != null && 
            (fromYear > toYear || (fromYear.equals(toYear) && fromMonth != null && toMonth != null && fromMonth > toMonth))) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_RANGE"));
            return prepareModelForBorrowingReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox);
        }

        // Lọc sách theo khoảng thời gian
        List<Book> books = bookService.getBorrowingBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId)
                                     .stream()
                                     .filter(book -> book.getBorrowCount() > 0)
                                     .collect(Collectors.toList());
        List<Category> categories = categoryService.getAllCategories();
        
        // Tính toán số liệu tổng quan
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream().mapToLong(Book::getBorrowCount).sum();
        long totalAvailable = books.stream().mapToLong(book -> book.getAmount() - book.getBorrowCount() - book.getIsDamaged()).sum();
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();

        // Lưu dữ liệu vào model
        model.addAttribute("books", books);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("fromMonth", fromMonth);
        model.addAttribute("fromYear", fromYear);
        model.addAttribute("toMonth", toMonth);
        model.addAttribute("toYear", toYear);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        
        return "borrowing_report";
    }

    // Top user mượn nhiều
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

    // Phương thức hỗ trợ để chuẩn bị model cho monthly_borrow khi có lỗi
    private String prepareModelForMonthlyBorrow(Model model, String query, Integer fromMonth, Integer fromYear, 
                                               Integer toMonth, Integer toYear, Integer categoryId, String selectedBox) {
        List<Book> books = bookService.getBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream().mapToLong(Book::getBorrowCount).sum();
        long totalAvailable = books.stream().mapToLong(book -> book.getAmount() - book.getBorrowCount() - book.getIsDamaged()).sum();
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();

        model.addAttribute("books", books);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("fromMonth", fromMonth);
        model.addAttribute("fromYear", fromYear);
        model.addAttribute("toMonth", toMonth);
        model.addAttribute("toYear", toYear);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        
        return "monthly_borrow";
    }

    // Phương thức hỗ trợ để chuẩn bị model cho borrowing_report khi có lỗi
    private String prepareModelForBorrowingReport(Model model, String query, Integer fromMonth, Integer fromYear, 
                                                 Integer toMonth, Integer toYear, Integer categoryId, String selectedBox) {
        List<Book> books = bookService.getBorrowingBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId)
                                     .stream()
                                     .filter(book -> book.getBorrowCount() > 0)
                                     .collect(Collectors.toList());
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream().mapToLong(Book::getBorrowCount).sum();
        long totalAvailable = books.stream().mapToLong(book -> book.getAmount() - book.getBorrowCount() - book.getIsDamaged()).sum();
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();

        model.addAttribute("books", books);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("fromMonth", fromMonth);
        model.addAttribute("fromYear", fromYear);
        model.addAttribute("toMonth", toMonth);
        model.addAttribute("toYear", toYear);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        
        return "borrowing_report";
    }
}
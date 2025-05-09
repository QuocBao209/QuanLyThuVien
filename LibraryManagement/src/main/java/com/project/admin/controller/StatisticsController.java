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

    // Hiện danh sách tổng
    @PostMapping("/statistics/all")
    public String monthlyBorrowForm(Model model,
                                    @RequestParam(value = "month", required = false) Integer month,
                                    @RequestParam(value = "year", required = false) Integer year,
                                    @RequestParam(required = false) String selectedBox,
                                    @RequestParam(required = false) String selected) {
        
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getBooksByMonthAndYear(null, month, year, month, year, null);
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        model.addAttribute("selected", selected);
        
        return "monthly_borrow";
    }

    // Xử lý toolbar ở danh sách tổng
    @PostMapping("/statistics/all/book-borrow-stats")
    public String getBorrowStats(@RequestParam(required = false) String query,
                                 @RequestParam(required = false) Integer fromMonth,
                                 @RequestParam(required = false) Integer fromYear,
                                 @RequestParam(required = false) Integer toMonth,
                                 @RequestParam(required = false) Integer toYear,
                                 @RequestParam(required = false) Integer categoryId,
                                 @RequestParam(required = false) String selectedBox,
                                 @RequestParam(required = false) String selected,
                                 Model model) {
        
        if ((fromMonth != null && fromYear == null) || (toMonth != null && toYear == null)) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_COMPLETENESS"));
            return prepareModelForMonthlyBorrow(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        if (fromYear != null && toYear != null && 
            (fromYear > toYear || (fromYear.equals(toYear) && fromMonth != null && toMonth != null && fromMonth > toMonth))) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_RANGE"));
            return prepareModelForMonthlyBorrow(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        List<Book> books = bookService.getBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
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
        model.addAttribute("selected", selected);
        
        return "monthly_borrow";
    }
    
    private String prepareModelForMonthlyBorrow(Model model, String query, Integer fromMonth, Integer fromYear, 
                                               Integer toMonth, Integer toYear, Integer categoryId, String selectedBox, String selected) {
        List<Book> books = bookService.getBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
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
        model.addAttribute("selected", selected);
        
        return "monthly_borrow";
    }

    // Hiện danh sách đang mượn
    @PostMapping("/statistics/borrowing")
    public String getBorrowingBook(Model model,
                                   @RequestParam(value = "month", required = false) Integer month,
                                   @RequestParam(value = "year", required = false) Integer year,
                                   @RequestParam(required = false) String selectedBox,
                                   @RequestParam(required = false) String selected) {
        
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getBorrowingBooksByMonthAndYear(null, month, year, month, year, null);
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        model.addAttribute("selected", selected);
        
        return "borrowing_report";
    }

    // Xử lý toolbar cho danh sách đang mượn
    @PostMapping("/statistics/borrowing/book-borrow-stats")
    public String getBorrowingStats(@RequestParam(required = false) String query,
                                    @RequestParam(required = false) Integer fromMonth,
                                    @RequestParam(required = false) Integer fromYear,
                                    @RequestParam(required = false) Integer toMonth,
                                    @RequestParam(required = false) Integer toYear,
                                    @RequestParam(required = false) Integer categoryId,
                                    @RequestParam(required = false) String selectedBox,
                                    @RequestParam(required = false) String selected,
                                    Model model) {
        
        if ((fromMonth != null && fromYear == null) || (toMonth != null && toYear == null)) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_COMPLETENESS"));
            return prepareModelForBorrowingReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        if (fromYear != null && toYear != null && 
            (fromYear > toYear || (fromYear.equals(toYear) && fromMonth != null && toMonth != null && fromMonth > toMonth))) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_RANGE"));
            return prepareModelForBorrowingReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        List<Book> books = bookService.getBorrowingBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
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
        model.addAttribute("selected", selected);
        
        return "borrowing_report";
    }
    
    private String prepareModelForBorrowingReport(Model model, String query, Integer fromMonth, Integer fromYear, 
                                                 Integer toMonth, Integer toYear, Integer categoryId, String selectedBox, String selected) {
        
        List<Book> books = bookService.getBorrowingBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
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
        model.addAttribute("selected", selected);
        
        return "borrowing_report";
    }

    // Hiện danh sách sẵn sàng
    @PostMapping("/statistics/ready")
    public String getReadyBook(Model model,
                               @RequestParam(value = "month", required = false) Integer month,
                               @RequestParam(value = "year", required = false) Integer year,
                               @RequestParam(required = false) String selectedBox,
                               @RequestParam(required = false) String selected) {
        
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getReadyBooksByMonthAndYear(null, month, year, month, year, null);
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        model.addAttribute("selected", selected);
        
        return "readyBook";
    }

    // Xử lý toolbar cho danh sách sẵn sàng
    @PostMapping("/statistics/ready/book-borrow-stats")
    public String getReadyStats(@RequestParam(required = false) String query,
                                @RequestParam(required = false) Integer fromMonth,
                                @RequestParam(required = false) Integer fromYear,
                                @RequestParam(required = false) Integer toMonth,
                                @RequestParam(required = false) Integer toYear,
                                @RequestParam(required = false) Integer categoryId,
                                @RequestParam(required = false) String selectedBox,
                                @RequestParam(required = false) String selected,
                                Model model) {
        
        if ((fromMonth != null && fromYear == null) || (toMonth != null && toYear == null)) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_COMPLETENESS"));
            return prepareModelForReadyReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        if (fromYear != null && toYear != null && 
            (fromYear > toYear || (fromYear.equals(toYear) && fromMonth != null && toMonth != null && fromMonth > toMonth))) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_RANGE"));
            return prepareModelForReadyReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        List<Book> books = bookService.getReadyBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
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
        model.addAttribute("selected", selected);
        
        return "readyBook";
    }
    
    private String prepareModelForReadyReport(Model model, String query, Integer fromMonth, Integer fromYear, 
                                             Integer toMonth, Integer toYear, Integer categoryId, String selectedBox, String selected) {
        
        List<Book> books = bookService.getReadyBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = books.stream().mapToLong(Book::getAmount).sum();
        long totalBorrowing = books.stream()
            .mapToLong(Book::getBorrowedRecordsCount)
            .sum();
        long totalAvailable = books.stream()
            .mapToLong(book -> book.getAmount() - book.getBorrowedRecordsCount() - book.getIsDamaged())
            .sum();
        totalAvailable = totalAvailable < 0 ? 0 : totalAvailable;
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
        model.addAttribute("selected", selected);
        
        return "readyBook";
    }

    // Hiện danh sách hư hại
    @PostMapping("/statistics/damaged")
    public String getDamagedBook(Model model,
                                 @RequestParam(value = "month", required = false) Integer month,
                                 @RequestParam(value = "year", required = false) Integer year,
                                 @RequestParam(required = false) String selectedBox,
                                 @RequestParam(required = false) String selected) {
        
        List<Category> categories = categoryService.getAllCategories();
        List<Book> books = bookService.getDamagedBooksByMonthAndYear(null, month, year, month, year, null);
        
        long totalBooks = bookService.getTotalBooks();
        long totalBorrowing = bookService.getTotalBorrowing(month, year);
        long totalAvailable = bookService.getTotalAvailable(month, year);
        long totalDamaged = books.stream().mapToLong(Book::getIsDamaged).sum();
        
        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);
        model.addAttribute("selectedBox", selectedBox);
        model.addAttribute("selected", selected);
        
        return "damagedBook";
    }

    // Xử lý toolbar cho danh sách hư hại
    @PostMapping("/statistics/damaged/book-borrow-stats")
    public String getDamagedStats(@RequestParam(required = false) String query,
                                  @RequestParam(required = false) Integer fromMonth,
                                  @RequestParam(required = false) Integer fromYear,
                                  @RequestParam(required = false) Integer toMonth,
                                  @RequestParam(required = false) Integer toYear,
                                  @RequestParam(required = false) Integer categoryId,
                                  @RequestParam(required = false) String selectedBox,
                                  @RequestParam(required = false) String selected,
                                  Model model) {
        
        if ((fromMonth != null && fromYear == null) || (toMonth != null && toYear == null)) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_COMPLETENESS"));
            return prepareModelForDamagedReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        if (fromYear != null && toYear != null && 
            (fromYear > toYear || (fromYear.equals(toYear) && fromMonth != null && toMonth != null && fromMonth > toMonth))) {
            model.addAttribute("errorMsg", AdminCodes.getErrorMessage("INVALID_DATE_RANGE"));
            return prepareModelForDamagedReport(model, query, fromMonth, fromYear, toMonth, toYear, categoryId, selectedBox, selected);
        }

        List<Book> books = bookService.getDamagedBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = bookService.getTotalBooks();
        long totalBorrowing = bookService.getTotalBorrowing(fromMonth != null && fromYear != null ? fromMonth : null, 
                                                            fromYear != null ? fromYear : null);
        long totalAvailable = bookService.getTotalAvailable(fromMonth != null && fromYear != null ? fromMonth : null, 
                                                            fromYear != null ? fromYear : null);
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
        model.addAttribute("selected", selected);
        
        return "damagedBook";
    }

    private String prepareModelForDamagedReport(Model model, String query, Integer fromMonth, Integer fromYear, 
                                               Integer toMonth, Integer toYear, Integer categoryId, String selectedBox, String selected) {
        
        List<Book> books = bookService.getDamagedBooksByMonthAndYear(query, fromMonth, fromYear, toMonth, toYear, categoryId);
        List<Category> categories = categoryService.getAllCategories();
        
        long totalBooks = bookService.getTotalBooks();
        long totalBorrowing = bookService.getTotalBorrowing(fromMonth != null && fromYear != null ? fromMonth : null, 
                                                            fromYear != null ? fromYear : null);
        long totalAvailable = bookService.getTotalAvailable(fromMonth != null && fromYear != null ? fromMonth : null, 
                                                            fromYear != null ? fromYear : null);
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
        model.addAttribute("selected", selected);
        
        return "damagedBook";
    }
}
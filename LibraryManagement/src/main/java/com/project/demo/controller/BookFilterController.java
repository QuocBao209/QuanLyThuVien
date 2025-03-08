package com.project.demo.controller;

import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/home")
public class BookFilterController {

	@Autowired private BookService bookService;
	@Autowired private CategoryService categoryService;

	public BookFilterController(BookService bookService, CategoryService categoryService) {
		this.bookService = bookService;
		this.categoryService = categoryService;
	}
	
	@GetMapping("/book-filter")
	public ModelAndView bookFilterPage(
	        @RequestParam(value = "categoryName", required = false) Set<String> categoryNames,
	        @RequestParam(value = "timeFilter", required = false) String timeRange,  // Đổi Set<String> thành String
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "20") int size) {

	    ModelAndView mav = new ModelAndView("bookFilter");

	    List<Category> categories = categoryService.getAllCategories();
	    Page<Book> bookPage = bookService.filterBooks(categoryNames, timeRange, page, size); // Đổi timeRanges thành timeRange

	    mav.addObject("bookPage", bookPage);
	    mav.addObject("currentPage", page);
	    mav.addObject("categories", categories);
	    mav.addObject("categoryNames", categoryNames);
	    mav.addObject("selectedTimeRange", timeRange);  // Thêm để kiểm tra giá trị đã chọn trong form
	    return mav;
	}

}

package com.project.demo.controller;

import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;

@Controller
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
			@RequestParam(value = "timeFilter", required = false) Set<String> timeRanges) {

		List<Category> categories = categoryService.getAllCategories();
		List<Book> books = bookService.filterBooks(categoryNames, timeRanges);

		ModelAndView mav = new ModelAndView("bookFilter");
		mav.addObject("categories", categories);
		mav.addObject("books", books);
		return mav;
	}
}

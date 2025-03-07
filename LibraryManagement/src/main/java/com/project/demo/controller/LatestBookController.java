package com.project.demo.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;

@Controller
@RequestMapping("/home")
public class LatestBookController {
	
	@Autowired private BookService bookService;
	@Autowired private CategoryService categoryService;
	 
	 public LatestBookController(BookService bookService, CategoryService categoryService) {
	    this.bookService = bookService;
	    this.categoryService = categoryService;
	 }
	@GetMapping("/latest-book")
    public ModelAndView latestBookPage(
    		@RequestParam(defaultValue = "0") int page,
    		@RequestParam(defaultValue = "5") int size){
        ModelAndView model = new ModelAndView("latestBook");
        List<Category> categories = categoryService.getAllCategories();
        Page<Book> bookPage = bookService.getLatestBooks(page, size);
        model.addObject("categories", categories);
        model.addObject("bookPage", bookPage);
        model.addObject("currentPage", page);
        return model;
    }
}

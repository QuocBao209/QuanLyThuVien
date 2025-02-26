package com.project.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Category;
import com.project.demo.service.CategoryService;

@Controller
@RequestMapping("/home")
public class BookFilterController {
	
	@Autowired private CategoryService categoryService;
	
	public BookFilterController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@GetMapping("/book-filter")
	public ModelAndView bookFilterPage() {
		List<Category> categories = categoryService.getAllCategories();
		ModelAndView mav = new ModelAndView("bookFilter");
		mav.addObject("categories", categories);
		return mav;
	}
}

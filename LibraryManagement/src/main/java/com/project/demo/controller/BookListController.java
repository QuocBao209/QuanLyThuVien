package com.project.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/admin")
public class BookListController {
	@GetMapping("/book-list")
	public ModelAndView showBookListForm() {
		ModelAndView modelAndView = new ModelAndView("bookList");
        return modelAndView;
	}
}

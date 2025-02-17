package com.project.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/home")
public class HomeController {
	@GetMapping("") 
	public ModelAndView showHomePage() {
		return new ModelAndView("home");
	}
	
	@PostMapping("/book-filter")
	public ModelAndView bookFilterPage() {
		return new ModelAndView("bookFilter");
	}
}

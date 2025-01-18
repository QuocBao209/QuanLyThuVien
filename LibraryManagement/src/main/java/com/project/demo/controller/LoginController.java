package com.project.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {
	// Hiển thị trang đăng nhập
	@GetMapping("/login")
	public ModelAndView showLoginPage() {
		ModelAndView modelAndView = new ModelAndView("login");
		return modelAndView;
	}
	
	// Xử lý đăng nhập
	@PostMapping("/login")
	public ModelAndView processLogin(@RequestParam("username") String username, @RequestParam("password") String password) {
		ModelAndView modelAndView = new ModelAndView();
		
		if("admin".equals(username) && "password".equals(password)) {
			modelAndView.setViewName("/home");
		} 
		else {
			modelAndView.addObject("error", "Invalid username or password");
			modelAndView.setViewName("login");
		}
		
		return modelAndView;
	}
}

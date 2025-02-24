package com.project.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.service.BookService;
import com.project.demo.service.UserService;

@Controller
@RequestMapping ("/admin")
public class UserController {
	
	@Autowired private UserService userService;
	
	// Hiển thị danh sách tài khoản độc giả
	@PostMapping("/user-list")
	public ModelAndView userListForm() {
		ModelAndView modelAndView = new ModelAndView("userList");
        modelAndView.addObject("users", userService.getUsers());
        return modelAndView;
	}
}

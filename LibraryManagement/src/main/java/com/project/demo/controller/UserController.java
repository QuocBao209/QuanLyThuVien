package com.project.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping ("/admin")
public class UserController {
	@GetMapping("/user-list")
	public ModelAndView userListForm() {
		return new ModelAndView("userList");
	}
}

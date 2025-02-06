package com.project.demo.controller;

import com.project.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LoginController {

	@Autowired
	private UserService userService;

	// Hiển thị trang đăng nhập
	@GetMapping("/login")
	public ModelAndView showLoginPage() {
		return new ModelAndView("login");
	}

	// Xử lý đăng nhập từ database
	@PostMapping("/login")
	public ModelAndView processLogin(@RequestParam("username") String username,
									 @RequestParam("password") String password) {
		ModelAndView modelAndView = new ModelAndView();

		if (userService.authenticateUser(username, password)) {
			modelAndView.setView(new RedirectView("/admin"));
		} else {
			modelAndView.addObject("error", "Invalid username or password");
			modelAndView.setViewName("login"); // Nếu đăng nhập thất bại
		}

		return modelAndView;
	}
}

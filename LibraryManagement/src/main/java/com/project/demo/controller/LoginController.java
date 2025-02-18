package com.project.demo.controller;

import com.project.demo.service.UserService;
import com.project.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

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
		Optional<User> userOptional = userService.authenticateUser(username, password);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			if ("ADMIN".equalsIgnoreCase(user.getRole())) {
				modelAndView.setViewName("admin");
			} else {
				modelAndView.setViewName("home");
			}
		} else {
			modelAndView.addObject("error", "Invalid username or password");
			modelAndView.setViewName("login");
		}

		return modelAndView;
	}


	@GetMapping("/login/forget-password")
	public String forgetPasswordForm() {
		return "forgetPassword";
	}
}

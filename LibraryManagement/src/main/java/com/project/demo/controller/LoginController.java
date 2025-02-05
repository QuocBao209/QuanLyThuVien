package com.project.demo.controller;

import com.project.demo.entity.User;
import com.project.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
			modelAndView.setViewName("home");
		} else {
			modelAndView.addObject("error", "Invalid username or password");
			modelAndView.setViewName("login");
		}

		return modelAndView;
	}

	// Hiển thị trang đăng ký
	@GetMapping("/register")
	public ModelAndView showRegisterPage() {
		ModelAndView modelAndView = new ModelAndView("register");
		return modelAndView;
	}

	// Xử lý đăng ký
	@PostMapping("/register")
	public ModelAndView processRegister(@RequestParam("cmt") String cmt,
										@RequestParam("name") String name,
										@RequestParam("phone") String phone,
										@RequestParam("email") String email,
										@RequestParam("username") String username,
										@RequestParam("password") String password) {
		ModelAndView modelAndView = new ModelAndView();

		// Tạo đối tượng User từ form đăng ký
		User newUser = new User();
		newUser.setCmt(cmt);
		newUser.setName(name);
		newUser.setPhone(phone);
		newUser.setEmail(email);
		newUser.setUsername(username);
		newUser.setPassword(password);
		newUser.setRole("USER");  // Mặc định role là USER, có thể thay đổi nếu cần

		// Gọi service để lưu người dùng vào cơ sở dữ liệu
		userService.registerUser(newUser);

		// Chuyển hướng về trang đăng nhập sau khi đăng ký thành công
		modelAndView.setViewName("login");
		modelAndView.addObject("message", "Registration successful. Please login.");

		return modelAndView;
	}
}

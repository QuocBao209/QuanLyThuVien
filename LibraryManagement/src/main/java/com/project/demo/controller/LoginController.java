package com.project.demo.controller;

import com.project.demo.service.UserService;
import com.project.demo.utils.UserCodes;
import jakarta.servlet.http.HttpSession;
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

	// Xử lý đăng nhập
	@PostMapping("/login")
	public ModelAndView login(@RequestParam("username") String username,
 				             @RequestParam("password") String password,
				             HttpSession session) {
		ModelAndView mav = new ModelAndView();
		Optional<User> userOptional = userService.authenticateUser(username, password);
		
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			
			// Kiểm tra trạng thái tài khoản
			if ("Khóa".equals(user.getStatus())) {
				mav.setViewName("account_locked");
			} else {
				// Lưu username và role vào session
				session.setAttribute("user", username);
				session.setAttribute("role", user.getRole());
				mav.setViewName("redirect:/home");
			}
		} else {
			System.out.println("Đăng nhập thất bại: " + username);
			mav.setViewName("login"); // Giữ nguyên trang đăng nhập
			mav.addObject("error", UserCodes.getErrorMessage("INVALID_CREDENTIALS_2"));
		}
		return mav;
	}

	// Đăng xuất tài khoản
	@GetMapping("/user-logout")
	public String logout(HttpSession session) {
		session.invalidate(); // Xóa toàn bộ session
		return "redirect:/home";
	}
}

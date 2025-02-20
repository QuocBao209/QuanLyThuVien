package com.project.demo.controller;

import com.project.demo.service.UserService;

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

	// Xử lý đăng nhập từ database
	@PostMapping("/login")
	public ModelAndView processLogin(@RequestParam("username") String username,
									 @RequestParam("password") String password,
									 HttpSession session) {
		
		ModelAndView modelAndView = new ModelAndView();
		Optional<User> userOptional = userService.authenticateUser(username, password);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			
			session.setAttribute("user", username);			// Lưu username vào session
			session.setAttribute("role", user.getRole());	// Lưu role vào session
			
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
	
	// Đăng xuất tài khoản user, xóa session
    @GetMapping("/user-logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa toàn bộ session
        return "redirect:/home"; // Quay về trangchủ
    }
    
    // Đăng xuất tài khoản admin, xóa session
    @GetMapping("logout")
    public String logoutAdmin(HttpSession session) {
    	session.invalidate();
    	return "redirect:/login";
    }

    // Quên mật khẩu
	@GetMapping("/login/forget-password")
	public String forgetPasswordForm() {
		return "forgetPassword";
	}
}

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
	
	
	// Đăng nhập trang Admin
	@PostMapping("/login")
	public ModelAndView loginAdminPage(@RequestParam("username") String username,
									   @RequestParam("password") String password,
									   HttpSession session) {
			ModelAndView mav = new ModelAndView();
			Optional<User> userOptionnal = userService.authenticateUser(username, password);
			
			if (userOptionnal.isPresent()) {
				User user = userOptionnal.get();
				
				session.setAttribute("user", username);
				session.setAttribute("role", user.getRole());
				
				mav.setViewName("home");
			}
			return mav;
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

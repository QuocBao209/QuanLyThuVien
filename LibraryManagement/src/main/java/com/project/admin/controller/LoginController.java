package com.project.admin.controller;

import com.project.admin.service.UserService;

import jakarta.servlet.http.HttpSession;

import com.project.admin.entity.User;
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
	
	@GetMapping("/admin-login")
	public ModelAndView showAdminLoginPage() {
		return new ModelAndView("adminLogin");
	}
	
	// Đăng nhập trang Admin
	@PostMapping("/admin-login")
	public ModelAndView loginAdminPage(@RequestParam("username") String username,
									   @RequestParam("password") String password,
									   HttpSession session) {
		ModelAndView mav = new ModelAndView();
		Optional<User> userOptional = userService.authenticateUser(username, password);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			session.setAttribute("userId", user.getUserId()); // Lưu userId vào session
			session.setAttribute("username", user.getUsername());
			session.setAttribute("role", user.getRole());

			mav.setViewName("admin");
		} else {
			mav.setViewName("adminLogin");
			mav.addObject("error", "Sai tài khoản hoặc mật khẩu!");
		}
		return mav;
	}


	// Đăng xuất tài khoản admin, xóa session
    @GetMapping("logout")
    public String logoutAdmin(HttpSession session) {
    	session.invalidate();
    	return "redirect:/admin-login";
    }

    // Quên mật khẩu
	@GetMapping("/login/forget-password")
	public String forgetPasswordForm() {
		return "forgetPassword";
	}
}
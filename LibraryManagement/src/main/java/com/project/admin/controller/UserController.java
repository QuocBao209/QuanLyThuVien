package com.project.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.admin.service.UserService;

@Controller
@RequestMapping ("/admin")
public class UserController {
	
	@Autowired private UserService userService;
	
	@PostMapping("/user-list")
	public ModelAndView userListForm(@RequestParam(value = "keyword", required = false) String keyword) {
		ModelAndView modelAndView = new ModelAndView("userList");

		if (keyword == null || keyword.isEmpty()) {
			modelAndView.addObject("users", userService.getAllUsersWithRoleUser());
		} else {
			modelAndView.addObject("users", userService.searchUsers(keyword));
		}

		return modelAndView;
	}

	// Cập nhật status
	@PostMapping("/update-status")
	public String updateStatus(@RequestParam("userId") Long userId, 
							   @RequestParam("status") String newStatus) {
		userService.updateStatus(userId, newStatus);
		return "forward:/admin/user-list";
	}

	// Xử lý trạng thái tài khoản
	@PostMapping("/confirm-toggle-status")
	public String toggleUserStatus(@RequestParam Long userId) {
	    userService.findById(userId).ifPresent(user -> {
	        user.setStatus(user.getStatus().equalsIgnoreCase("Hoạt động") ? "Khóa" : "Hoạt động");
	        userService.save(user);
	    });
	    return "forward:/admin/user-list";
	}
}

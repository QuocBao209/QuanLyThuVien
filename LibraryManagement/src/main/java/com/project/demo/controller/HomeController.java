package com.project.demo.controller;

import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import com.project.demo.service.NotificationService;
import com.project.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/home")
public class HomeController {
	private final NotificationService notificationService;
	private final UserService userService;

	public HomeController(NotificationService notificationService, UserService userService) {
		this.notificationService = notificationService;
		this.userService = userService;
	}
	@GetMapping("")
	public String showHomePage(Model model, Principal principal) {
		if (principal != null) {
			Optional<User> userOptional = userService.getUserByUsername(principal.getName());
			User user = userOptional.orElse(null); // Lấy User hoặc null
			if (user != null) {
				List<Notification> notifications = notificationService.getNotificationsByUser(Optional.of(user));
				model.addAttribute("notifications", notifications);
			}
		}
		return "home";
	}

}

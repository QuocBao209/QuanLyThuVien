package com.project.demo.controller;

import com.project.demo.entity.Notification;
import com.project.demo.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/home")
public class HomeController {
	private final NotificationService notificationService;

	public HomeController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("")
	public String showHomePage(Model model, HttpSession session) {
		String username = (String) session.getAttribute("loggedInUser");

		if (username != null) {
			List<Notification> notifications = notificationService.getNotificationsByUsername(username);
			model.addAttribute("notifications", notifications);
		}

		return "home";
	}
}

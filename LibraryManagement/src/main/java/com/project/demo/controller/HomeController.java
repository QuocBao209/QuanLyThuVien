package com.project.demo.controller;

import com.project.demo.entity.Notification;
import com.project.demo.repository.NotificationRepository;
import com.project.demo.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/home")
public class HomeController {
	private final NotificationService notificationService;
	
	private final NotificationRepository notificationRepository;

	public HomeController(NotificationService notificationService, NotificationRepository notificationRepository) {
		this.notificationService = notificationService;
		this.notificationRepository = notificationRepository;
	}

	@GetMapping("")
	public String showHomePage(Model model, HttpSession session, HttpServletRequest request) {
		String username = (String) session.getAttribute("user");

		if (username != null) {
			List<Notification> notifications = notificationService.getNotificationsByUsername(username);
			model.addAttribute("notifications", notifications);
			
			// Sắp xếp danh sách: theo thời gian createdAt từ mới nhất đến cũ nhất
	        notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
			
			// Đếm số thông báo chưa đọc
		    long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
		    model.addAttribute("unreadCount", unreadCount);
		}
		
		// Thêm requestURI vào model
	    model.addAttribute("currentUrl", request.getRequestURI());
	    
		return "home";
	}
	
	@PostMapping("/mark-notification-read")
	public String markNotificationRead(
	        @RequestParam("notificationId") Integer notificationId,
	        @RequestParam(value = "redirectUrl", defaultValue = "/home") String redirectUrl,
	        HttpSession session) {
	    String username = (String) session.getAttribute("user");
	    if (username == null) {
	        return "redirect:/login";
	    }

	    Notification notification = notificationRepository.findById(notificationId).orElse(null);
	    if (notification != null) {
	        notification.setRead(true);
	        notificationRepository.save(notification);
	    }
	    return "redirect:" + redirectUrl; // Quay lại trang hiện tại
	}

}

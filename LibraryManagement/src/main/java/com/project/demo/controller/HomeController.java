package com.project.demo.controller;

import com.project.demo.entity.User;
import com.project.demo.entity.Notification;
import com.project.demo.repository.NotificationRepository;
import com.project.demo.service.NotificationService;
import com.project.demo.service.UserService;
import com.project.demo.utils.UserCodes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/home")
public class HomeController {
	private final NotificationService notificationService;
	
	private final NotificationRepository notificationRepository;
	
	private final UserService userService;

	public HomeController(NotificationService notificationService, NotificationRepository notificationRepository, UserService userService) {
		this.notificationService = notificationService;
		this.notificationRepository = notificationRepository;
		this.userService = userService;
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
	
	@GetMapping("/notifications")
    public ModelAndView notificationsPage(HttpSession session, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("notifications");

        String username = (String) session.getAttribute("user");
        if (username != null) {
        	Optional<User> userOptional = userService.getUserByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Lấy danh sách thông báo
                List<Notification> notifications = notificationService.getNotificationsByUsername(username);
                notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

                // Đếm số lượng thông báo chưa đọc
                long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

                // Kiểm tra nếu không có thông báo
                if (notifications.isEmpty()) {
                    mav.addObject("noNotifications", UserCodes.getErrorMessage("NO_NOTIFICATIONS"));
                }
                
                // Thêm requestURI vào model
        	    mav.addObject("currentUrl", request.getRequestURI());
                mav.addObject("user", user);
                mav.addObject("notifications", notifications);
                mav.addObject("unreadCount", unreadCount);
            }
        }

        return mav;
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

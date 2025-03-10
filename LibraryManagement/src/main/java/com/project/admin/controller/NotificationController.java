package com.project.admin.controller;

import com.project.admin.entity.Notification;
import com.project.admin.entity.User;
import com.project.admin.service.NotificationService;
import com.project.admin.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public String showNotifications(Model model) {
        // Giả sử có user đăng nhập
        User currentUser = userService.getCurrentUser();

        // Lấy danh sách thông báo
        List<Notification> notifications = notificationService.getAllNotifications(currentUser);
        model.addAttribute("notifications", notifications);

        return "notifications";
    }

    @PostMapping("/mark-read/{id}")
    public String markNotificationAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }
}

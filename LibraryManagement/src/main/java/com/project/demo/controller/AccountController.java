package com.project.demo.controller;

import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import com.project.demo.service.Borrow_ReturnService;
import com.project.demo.service.NotificationService;
import com.project.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/home")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private Borrow_ReturnService borrowService;
    
    @Autowired
    private NotificationService notificationService;
    
    public AccountController(NotificationService notificationService) {
    	this.notificationService = notificationService;
    }

    // Xử lý hiện thông tin của User và hiện thông tin sách trên table
    @GetMapping("/account")
    public ModelAndView accountForm(HttpSession session, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("account");

        String username = (String) session.getAttribute("user");
        if (username == null) {
            mav.setViewName("redirect:/login");
            return mav;
        }
        
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (!userOptional.isPresent()) {
            mav.setViewName("redirect:/login");
            return mav;
        }

        User user = userOptional.get();
        List<Notification> notifications = notificationService.getNotificationsByUsername(username);
        
        // Sắp xếp danh sách: theo thời gian createdAt từ mới nhất đến cũ nhất
        notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
        
        mav.addObject("user", user);
        mav.addObject("borroweBooks", borrowService.getBorrowsByUser(user));
        mav.addObject("notifications", notifications);
        
        // Thêm unreadCount
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        mav.addObject("unreadCount", unreadCount);
        
        // Thêm requestURI vào model
        mav.addObject("currentUrl", request.getRequestURI());
        
        return mav;
    }

    @PostMapping("/edit-account")
    public String editAccount(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        if (user.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: ID không hợp lệ!");
            return "redirect:/home/account";
        }

        Optional<User> userOptional = userService.getUserById(user.getUserId());

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            userService.updateUser(existingUser);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
        }

        return "redirect:/home/account";
    }
}

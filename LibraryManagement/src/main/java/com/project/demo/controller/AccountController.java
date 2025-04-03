package com.project.demo.controller;

import com.project.admin.utils.AdminCodes;
import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import com.project.demo.service.Borrow_ReturnService;
import com.project.demo.service.NotificationService;
import com.project.demo.service.UserService;

import com.project.demo.utils.UserCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        mav.addObject("borrowedBooks", getSortedBorrowReturns(user));
        mav.addObject("notifications", notifications);
        
        // Thêm unreadCount
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        mav.addObject("unreadCount", unreadCount);
        
        // Thêm requestURI vào model
        mav.addObject("currentUrl", request.getRequestURI());
        
        return mav;
    }
    
    // Sắp xếp thứ tự theo trạng thái mượn / trả
    private List<Borrow_Return> getSortedBorrowReturns(User user) {
        return borrowService.getBorrowsByUser(user)
            .stream()
            .sorted(Comparator
                .comparing((Borrow_Return br) -> br.getUserConfirmDate() == null ? 0 : 1)
                .thenComparing(br -> getStatusOrder(br.getStatus()))
            )
            .collect(Collectors.toList());
    }

    private int getStatusOrder(String status) {
        switch (status) {
            case "pending": return 1;
            case "borrowed": return 2;
            case "returned": return 3;
            case "outdate": return 4;
            default: return 5;
        }
    }

    // Chỉnh sửa tài khoản
    @PostMapping("/edit-account")
    public String editAccount(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        if (user.getUserId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", UserCodes.getErrorMessage("INVALID_USER_ID"));
            return "redirect:/home/account";
        }

        Optional<User> userOptional = userService.getUserById(user.getUserId());

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            userService.updateUser(existingUser);

            redirectAttributes.addFlashAttribute("successMessage", UserCodes.getErrorMessage("UPDATE_SUCCESS"));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", UserCodes.getErrorMessage("USER_NOT_FOUND"));
        }

        return "redirect:/home/account";
    }
}

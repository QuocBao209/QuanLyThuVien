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
    public ModelAndView accountForm(
            HttpSession session,
            HttpServletRequest request,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {

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

        // Lấy danh sách đã mượn và lọc theo keyword + status nếu có
        List<Borrow_Return> borrowedBooks = borrowService.findBorrowHistory(user.getUserId(), keyword, status);
        
        if (borrowedBooks.isEmpty()) {
        	mav.addObject("errorMessage", AdminCodes.getErrorMessage("BOOK_NOT_FOUND")); 
        }

        List<Notification> notifications = notificationService.getNotificationsByUsername(username);
        notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

        mav.addObject("user", user);
        mav.addObject("borrowedBooks", borrowedBooks);
        mav.addObject("notifications", notifications);
        mav.addObject("unreadCount", unreadCount);
        mav.addObject("currentUrl", request.getRequestURI());
        mav.addObject("keyword", keyword);
        mav.addObject("status", status);

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

    @PostMapping("/edit-account")
    public String editAccount(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        boolean hasError = false;

//        if (user.getUserId() == null) {
//            redirectAttributes.addFlashAttribute("errorUserId", UserCodes.getErrorMessage("INVALID_USER_ID"));
//            hasError = true;
//        }

        if (user.getName() == null || !user.getName().matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$")) {
            redirectAttributes.addFlashAttribute("errorName", UserCodes.getErrorMessage("INVALID_NAME_FORMAT_3"));
            hasError = true;
        }

        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            redirectAttributes.addFlashAttribute("errorEmail", UserCodes.getErrorMessage("INVALID_GMAIL_FORMAT_3"));
            hasError = true;
        }

        if (user.getPhone() == null || !user.getPhone().matches("^[0-9]{10}$")) {
            redirectAttributes.addFlashAttribute("errorPhone", UserCodes.getErrorMessage("INVALID_PHONE_FORMAT_3"));
            hasError = true;
        }

        if (hasError) {
            // Gửi lại dữ liệu nhập và bật popup
            redirectAttributes.addFlashAttribute("user", user);
            redirectAttributes.addFlashAttribute("openPopup", true);
            return "redirect:/home/account";
        }

        Optional<User> userOptional = userService.getUserById(user.getUserId());

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());
            existingUser.setPhone(user.getPhone());
            userService.updateUser(existingUser);

            redirectAttributes.addFlashAttribute("successMessage", UserCodes.getSuccessMessage("UPDATE_SUCCESS"));
        } else {
            redirectAttributes.addFlashAttribute("errorUserNotFound", UserCodes.getErrorMessage("USER_NOT_FOUND"));
        }

        return "redirect:/home/account";
    }
}

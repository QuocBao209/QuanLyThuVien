package com.project.demo.controller;

import com.project.demo.entity.User;
import com.project.demo.service.Borrow_ReturnService;
import com.project.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/home")
public class AccountController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private Borrow_ReturnService borrowService;
    
    
    // Xử lý hiện thông tin của User và hiện thông tin sách trên table (Bảo)
    @GetMapping("/account")
    public ModelAndView accountForm(HttpSession session) {
        ModelAndView mav = new ModelAndView("account");

        String username = (String) session.getAttribute("user");
        if (username == null) {
            mav.setViewName("redirect:/login");
            return mav;
        }

        Optional<User> userOptional = userService.getUserByUsername(username);
        userOptional.ifPresent(user -> {
            mav.addObject("user", user);
            mav.addObject("borrowesBooks", borrowService.getBorrowsByUser(user));
        });

        return mav;
    }

}

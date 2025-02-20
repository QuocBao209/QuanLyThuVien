package com.project.demo.controller;

import com.project.demo.entity.User;
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

    @GetMapping("/account")
    public ModelAndView accountForm(HttpSession session) {
        ModelAndView mav = new ModelAndView("account");

        // Lấy thông tin user từ username đã đăng nhập
        String username = (String) session.getAttribute("user"); 
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (username == null) {
            mav.setViewName("redirect:/login"); // Chuyển hướng về trang đăng nhập nếu chưa đăng nhập
            return mav;
        }

        userOptional.ifPresent(user -> mav.addObject("user", user));

        return mav;
    }
}

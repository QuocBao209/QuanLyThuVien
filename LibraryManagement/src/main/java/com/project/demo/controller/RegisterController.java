package com.project.demo.controller;

import com.project.demo.entity.User;
import com.project.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RegisterController {

    @Autowired
    private UserService userService;

    // Hiển thị trang đăng ký
    @GetMapping("/register")
    public ModelAndView showRegisterPage() {
        return new ModelAndView("register");
    }

    // Xử lý đăng ký người dùng mới
    @PostMapping("/register")
    public ModelAndView processRegister(@RequestParam("cmt") String cmt,
                                        @RequestParam("name") String name,
                                        @RequestParam("phone") String phone,
                                        @RequestParam("email") String email,
                                        @RequestParam("username") String username,
                                        @RequestParam("password") String password) {
        ModelAndView modelAndView = new ModelAndView();

        if (userService.existsByUsername(username)) {
            modelAndView.addObject("error", "Username already exists. Please choose another.");
            modelAndView.setViewName("register");
            return modelAndView;
        }

        User newUser = new User();
        newUser.setCmt(cmt);
        newUser.setName(name);
        newUser.setPhone(phone);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole("USER");
        newUser.setStatus("Hoạt động");

        userService.registerUser(newUser);

        modelAndView.setViewName("login");
        modelAndView.addObject("message", "Registration successful. Please login.");

        return modelAndView;
    }
}

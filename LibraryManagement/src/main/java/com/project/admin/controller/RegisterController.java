package com.project.admin.controller;

import com.project.admin.entity.User;
import com.project.admin.service.UserService;
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
    
    // Xử lý đăng ký Admin 
    // Hiển thị trang đăng ký
    @GetMapping("/admin-register")
    public ModelAndView showAdminRegisterPage() {
        return new ModelAndView("adminRegister");
    }

    // Xử lý đăng ký người dùng mới
    @PostMapping("/admin-register")
    public ModelAndView processAdminRegister(@RequestParam("cmt") String cmt,
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
        newUser.setRole("ADMIN");
        newUser.setStatus("Hoạt động");

        userService.registerUser(newUser);

        modelAndView.setViewName("adminLogin");
        modelAndView.addObject("message", "Registration successful. Please login.");

        return modelAndView;
    }
}
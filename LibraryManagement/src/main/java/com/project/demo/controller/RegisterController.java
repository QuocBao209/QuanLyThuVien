package com.project.demo.controller;

import com.project.admin.utils.AdminCodes;
import com.project.demo.entity.User;
import com.project.demo.service.UserService;
import com.project.demo.utils.UserCodes;
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
            modelAndView.addObject("error", UserCodes.getErrorMessage("INVALID_NAME_FORMAT_2"));
            modelAndView.setViewName("register");
            return modelAndView;
        }
        // Kiểm tra tên không chứa số
        if (!name.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$")) {
            modelAndView.addObject("error", UserCodes.getErrorMessage("INVALID_NAME_FORMAT_2"));
            return modelAndView;
        }

        // Kiểm tra định dạng số điện thoại (10 số)
        if (!phone.matches("\\d{10}")) {
            modelAndView.addObject("error", UserCodes.getErrorMessage("INVALID_PHONE_FORMAT_2"));
            return modelAndView;
        }

        // Kiểm tra định dạng CMT (12 số)
        if (!cmt.matches("\\d{12}")) {
            modelAndView.addObject("error", UserCodes.getErrorMessage("INVALID_CMT_FORMAT_2"));
            return modelAndView;
        }

        // Viết hoa chữ cái đầu của mỗi từ trong tên
        name = capitalizeEachWord(name);

        // Kiểm tra username có tồn tại không
        if (userService.existsByUsername(username)) {
            modelAndView.addObject("error", UserCodes.getErrorMessage("USERNAME_EXISTS_2"));
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
        modelAndView.addObject("message", UserCodes.getSuccessMessage("REGISTER_SUCCESS_2"));

        return modelAndView;
    }

    private String capitalizeEachWord(String str) {
        String[] words = str.trim().split("\\s+");
        StringBuilder capitalizedString = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return capitalizedString.toString().trim();
    }
}

package com.project.admin.controller;

import com.project.admin.entity.User;
import com.project.admin.service.UserService;
import com.project.admin.utils.AdminCodes;
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
        ModelAndView modelAndView = new ModelAndView("adminRegister");

        // Kiểm tra tên không chứa số
        if (!name.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]+$")) {
            modelAndView.addObject("errorName", AdminCodes.getErrorMessage("INVALID_NAME_FORMAT_1"));
            return modelAndView;
        }

        // Kiểm tra định dạng số điện thoại (10 số)
        if (!phone.matches("\\d{10}")) {
            modelAndView.addObject("errorPhone", AdminCodes.getErrorMessage("INVALID_PHONE_FORMAT_1"));
            return modelAndView;
        }

        // Kiểm tra định dạng CMT (12 số)
        if (!cmt.matches("\\d{12}")) {
            modelAndView.addObject("errorCMT", AdminCodes.getErrorMessage("INVALID_CMT_FORMAT_1"));
            return modelAndView;
        }

        // Viết hoa chữ cái đầu của mỗi từ trong tên
        name = capitalizeEachWord(name);

        // Kiểm tra username có tồn tại không
        if (userService.existsByUsername(username)) {
            modelAndView.addObject("errorUsername", AdminCodes.getErrorMessage("USERNAME_EXISTS_1"));
            return modelAndView;
        }

        // Tạo đối tượng User và lưu vào database
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
        //modelAndView.addObject("successMessage", AdminCodes.getSuccessMessage("REGISTER_SUCCESS_1"));
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
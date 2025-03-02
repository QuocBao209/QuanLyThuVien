package com.project.admin.controller;

import com.project.admin.service.EmailService;
import com.project.admin.service.PasswordResetService;
import com.project.admin.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ForgotPasswordController {
    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @GetMapping("/forget-password")
    public String showForgotPasswordPage(@RequestParam(required = false, defaultValue = "user") String from, Model model) {
        model.addAttribute("from", from); // Đưa `from` vào model để Thymeleaf sử dụng
        return "forgetPassword";
    }


    @PostMapping("/forget-password")
    public String sendOTP(@RequestParam String email, Model model) {
        String otp = passwordResetService.generateOTP();
        passwordResetService.saveOTP(email, otp);

        try {
            emailService.sendEmail(email, "Mã OTP đặt lại mật khẩu", "Mã OTP của bạn: " + otp);
            model.addAttribute("message", "Mã OTP đã được gửi đến email của bạn!");
            model.addAttribute("email", email);
            model.addAttribute("otpSent", true);
        } catch (MessagingException e) {
            model.addAttribute("error", "Gửi email thất bại!");
        }

        return "forgetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String otp,
                                @RequestParam String newPassword, Model model) {
        if (!passwordResetService.verifyOTP(email, otp)) {
            model.addAttribute("error", "Mã OTP không hợp lệ hoặc đã hết hạn!");
        } else if (userService.resetPassword(email, newPassword)) {
            model.addAttribute("message", "Mật khẩu đã được đặt lại thành công!");
            return "adminLogin";
        } else {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
        }

        model.addAttribute("email", email);
        model.addAttribute("otpSent", true);
        return "forgetPassword";
    }
    
    @GetMapping("/return")
    public String returnToLogin(@RequestParam(required = false, defaultValue = "user") String from) {
        return from.equals("admin") ? "redirect:/admin-login" : "redirect:/login";
    }

}
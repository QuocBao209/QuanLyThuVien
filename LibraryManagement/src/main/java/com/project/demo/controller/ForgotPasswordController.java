package com.project.demo.controller;

import com.project.admin.utils.AdminCodes;
import com.project.demo.service.EmailService;
import com.project.demo.service.PasswordResetService;
import com.project.demo.service.UserService;
import com.project.demo.utils.UserCodes;
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
    public String showForgotPasswordPage() {
        return "forgetPassword";
    }

    @PostMapping("/forget-password")
    public String sendOTP(@RequestParam String email, Model model) {
        // Kiểm tra email có tồn tại trong hệ thống hay không
        if (!userService.emailExists(email)) {
            model.addAttribute("error", AdminCodes.getErrorMessage("EMAIL_NOT_FOUND_1"));
            return "forgetPassword";
        }

        String otp = passwordResetService.generateOTP();
        passwordResetService.saveOTP(email, otp);

        try {
            emailService.sendEmail(email, "Mã OTP đặt lại mật khẩu", "Mã OTP của bạn: " + otp);
            model.addAttribute("message", UserCodes.getSuccessMessage("SUCCESS_CODE_2"));
            model.addAttribute("email", email);
            model.addAttribute("otpSent", true);
        } catch (MessagingException e) {
            model.addAttribute("error",  UserCodes.getErrorMessage("ERROR_CODE_2"));
        }

        return "forgetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String otp,
                                @RequestParam String newPassword, Model model) {
        if (!passwordResetService.verifyOTP(email, otp)) {
            model.addAttribute("resetError",  UserCodes.getErrorMessage("INVALID_OTP_2"));
        } else if (userService.resetPassword(email, newPassword)) {
        	String role = userService.getUserRole(email);
            model.addAttribute("resetMessage",  UserCodes.getSuccessMessage("PASSWORD_RESET_SUCCESS_2"));
            return role.equals("USER") ? "login" : "adminLogin";
        } else {
            model.addAttribute("resetError", UserCodes.getErrorMessage("EMAIL_NOT_FOUND_2"));
        }

        model.addAttribute("email", email);
        model.addAttribute("otpSent", true);
        return "forgetPassword";
    }
    
    @GetMapping("/return")
    public String returnToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/admin-return")
    public String returnToAdminLogin() {
        return "redirect:/admin-login";
    }
}

package com.project.admin.controller;

import com.project.admin.service.EmailService;
import com.project.admin.service.PasswordResetService;
import com.project.admin.service.UserService;
import com.project.admin.utils.AdminCodes;
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
        model.addAttribute("from", from);
        return "forgetPassword";
    }


    @PostMapping("/forget-password")
    public String sendOTP(@RequestParam String email, Model model) {
        // Kiểm tra xem email có tồn tại trong cơ sở dữ liệu không
        if (!userService.existsByEmail(email)) {
            model.addAttribute("errorEmail", AdminCodes.getErrorMessage("EMAIL_NOT_FOUND_1"));
            return "forgetPassword";
        }
        
        // Nếu email tồn tại, tiếp tục xử lý gửi OTP
        String otp = passwordResetService.generateOTP();
        passwordResetService.saveOTP(email, otp);

        try {
            emailService.sendEmail(email, "Mã OTP đặt lại mật khẩu", "Mã OTP của bạn: " + otp);
            model.addAttribute("successCode", AdminCodes.getSuccessMessage("SUCCESS_CODE_1"));
            model.addAttribute("email", email);
            model.addAttribute("otpSent", true);
        } catch (MessagingException e) {
            model.addAttribute("errorCode", AdminCodes.getErrorMessage("ERROR_CODE_1"));
        }

        return "forgetPassword";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String otp,
                                @RequestParam String newPassword, Model model) {
        if (!passwordResetService.verifyOTP(email, otp)) {
            model.addAttribute("invalidOTP", AdminCodes.getErrorMessage("INVALID_OTP_1"));
        } else if (userService.resetPassword(email, newPassword)) {
            model.addAttribute("successPassword", AdminCodes.getSuccessMessage("PASSWORD_RESET_SUCCESS_1"));
            return "adminLogin";
        } else {
            model.addAttribute("errorEmail", AdminCodes.getErrorMessage("EMAIL_NOT_FOUND_1"));
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
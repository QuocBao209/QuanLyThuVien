package com.project.demo.controller;

import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.User;
import com.project.demo.repository.Borrow_ReturnRepository;

import com.project.demo.repository.UserRepository;
import com.project.demo.utils.UserCodes;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/home")
public class BorrowReturnController {

    @Autowired
    private Borrow_ReturnRepository borrowReturnRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/confirm-borrow")
    public String confirmBorrow(@RequestParam("borrowId") Long borrowId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (borrowId == null) {
            return "redirect:/home/account";
        }

        Borrow_Return borrowReturn = borrowReturnRepository.findById(borrowId).orElse(null);
        if (borrowReturn != null) {

            // Lấy username từ session
            String username = (String) session.getAttribute("user");

            if (username == null) {
                return "redirect:/home/login";
            }

            // Tìm đối tượng User từ username trong cơ sở dữ liệu
            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isEmpty()) {
                return "redirect:/home/login";
            }

            User user = optionalUser.get();

            // Kiểm tra số lượng sách đã mượn của người dùng
            long borrowedBooksCount = borrowReturnRepository.countByUserAndStatus(user, "borrowed");

            if (borrowedBooksCount >= 3) {
            	redirectAttributes.addFlashAttribute("limitBorrow", UserCodes.getErrorMessage("INVLID_BORROW_ID_1"));
                return "redirect:/home/account";
            }

            // Nếu chưa đạt giới hạn, tiếp tục xác nhận mượn sách
            LocalDate userConfirmDate = LocalDate.now();
            borrowReturn.setUserConfirmDate(java.util.Date.from(userConfirmDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            borrowReturnRepository.save(borrowReturn);

            redirectAttributes.addFlashAttribute("message", UserCodes.getSuccessMessage("BORROW_REQUEST_SENT"));
        }

        return "redirect:/home/account";
    }

    @PostMapping("/delete-borrow")
    public String deleteBorrow(@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
        if (borrowId == null) {
            return "redirect:/home/account";
        }

        Borrow_Return borrowReturn = borrowReturnRepository.findById(borrowId).orElse(null);
        if (borrowReturn != null) {
            borrowReturnRepository.delete(borrowReturn); // Xóa yêu cầu mượn khỏi DB 

            redirectAttributes.addFlashAttribute("deleteSuccess", UserCodes.getSuccessMessage("SUCCESS_DELETE"));
        } 
        
        return "redirect:/home/account"; // ⚡ Chuyển hướng về trang danh sách mượn
    }
    
    
    @PostMapping("/renew")
    public String renewBook (@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
    	Borrow_Return borrowReturn = borrowReturnRepository.findById(borrowId).orElse(null);
    	
    	if (!"borrowed".equals(borrowReturn.getStatus())) {
            return "redirect:/home/account";
    	}
    	
    	// Tăng số lần gia hạn
    	borrowReturn.setRenewCount(borrowReturn.getRenewCount() + 1);
    	
    	// Lưu vào DB
        borrowReturnRepository.save(borrowReturn);

        redirectAttributes.addFlashAttribute("renewSuccess",  UserCodes.getSuccessMessage("RENEWAL_SUCCESS"));
        return "redirect:/home/account";
    }
}

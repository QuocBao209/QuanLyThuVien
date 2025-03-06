package com.project.demo.controller;

import com.project.demo.entity.Borrow_Return;
import com.project.demo.repository.Borrow_ReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/home")
public class BorrowReturnController {

    @Autowired
    private Borrow_ReturnRepository borrowReturnRepository;

    // ⚡ Đổi đường dẫn để tránh trùng với AccountController
    @GetMapping("/borrow-list")
    public String borrowListPage(Model model) {
        List<Borrow_Return> borrowList = borrowReturnRepository.findAll();
        model.addAttribute("borrowedBooks", borrowList);
        return "borrow-list"; // ⚡ Tên view cũng đổi cho rõ ràng hơn
    }

    @PostMapping("/confirm-borrow")
    public String confirmBorrow(@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
        if (borrowId == null) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: ID mượn sách không hợp lệ.");
            return "redirect:/home/account";
        }

        Borrow_Return borrowReturn = borrowReturnRepository.findById(borrowId).orElse(null);
        if (borrowReturn != null) {
        	// Lưu ngày User bấm Xác nhận mượn
        	LocalDate userConfirmDate = LocalDate.now();
            borrowReturn.setUserConfirmDate(java.util.Date.from(userConfirmDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            borrowReturnRepository.save(borrowReturn); // Lưu vào DB ✅

            redirectAttributes.addFlashAttribute("message", "Yêu cầu mượn sách đã được gửi! Vui lòng chờ admin duyệt.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Lỗi: Không tìm thấy thông tin mượn sách.");
        }

        return "redirect:/home/account"; // ⚡ Chuyển hướng về trang danh sách mượn
    }
}

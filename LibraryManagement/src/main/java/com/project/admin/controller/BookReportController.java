package com.project.admin.controller;

import com.project.admin.service.BookBorrowStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/statistics")
public class BookReportController {

    @Autowired
    private BookBorrowStatsService bookBorrowStatsService;

    @PostMapping("/book-report")
    public String showBookReport(Model model,
                                 @RequestParam(value = "month", required = false) Integer month,
                                 @RequestParam(value = "year", required = false) Integer year) {
        // Lấy dữ liệu từ service
        Map<String, Integer> borrowStatsByCategory = bookBorrowStatsService.getBorrowStatsByCategory(month, year);

        // Tổng quan số liệu
        int totalBorrowed = bookBorrowStatsService.getTotalBorrowed(month, year);
        int totalAvailable = bookBorrowStatsService.getTotalAvailable(month, year);
        int totalDamaged = bookBorrowStatsService.getTotalDamaged();

        // Dữ liệu cho biểu đồ (số lượng sách mượn theo thể loại)
        List<String> labels = new ArrayList<>(borrowStatsByCategory.keySet()); // Thể loại sách
        List<Integer> data = new ArrayList<>(borrowStatsByCategory.values()); // Số lượng sách mượn theo thể loại

        // Thêm dữ liệu vào model
        model.addAttribute("labels", labels);
        model.addAttribute("data", data);
        model.addAttribute("totalBorrowed", totalBorrowed);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);

        return "book_report"; // Trả về view Thymeleaf
    }
}
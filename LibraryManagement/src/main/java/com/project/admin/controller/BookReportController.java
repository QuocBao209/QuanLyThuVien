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
        Map<String, Integer> borrowingStatsByCategory = bookBorrowStatsService.getBorrowStatsByCategory(month, year);
        Map<String, Integer> totalBooksByCategory = bookBorrowStatsService.getTotalBooksByCategory();
        Map<String, Integer> damagedBooksByCategory = bookBorrowStatsService.getDamagedBooksByCategory();

        // Tổng quan số liệu
        int totalBooks = bookBorrowStatsService.getTotalBooks();
        int totalBorrowing = bookBorrowStatsService.getTotalBorrowing(month, year);
        int totalAvailable = bookBorrowStatsService.getTotalAvailable(month, year);
        int totalDamaged = bookBorrowStatsService.getTotalDamaged();

        // Dữ liệu cho biểu đồ
        List<String> labels = new ArrayList<>(borrowingStatsByCategory.keySet());
        List<Integer> borrowingData = new ArrayList<>();
        List<Integer> totalBooksData = new ArrayList<>();
        List<Integer> damagedBooksData = new ArrayList<>();

        for (String category : labels) {
            borrowingData.add(borrowingStatsByCategory.getOrDefault(category, 0));
            totalBooksData.add(totalBooksByCategory.getOrDefault(category, 0));
            damagedBooksData.add(damagedBooksByCategory.getOrDefault(category, 0));
        }

        // Thêm dữ liệu vào model
        model.addAttribute("labels", labels);
        model.addAttribute("borrowingData", borrowingData);
        model.addAttribute("totalBooksData", totalBooksData);
        model.addAttribute("damagedBooksData", damagedBooksData);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("totalBorrowing", totalBorrowing);
        model.addAttribute("totalAvailable", totalAvailable);
        model.addAttribute("totalDamaged", totalDamaged);

        return "book_report";
    }
}

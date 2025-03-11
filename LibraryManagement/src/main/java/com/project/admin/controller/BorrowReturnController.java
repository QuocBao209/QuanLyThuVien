package com.project.admin.controller;

import com.project.admin.entity.Borrow_Return;
import com.project.admin.entity.Notification;
import com.project.admin.service.Borrow_ReturnService;
import com.project.admin.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class BorrowReturnController {
	private final Borrow_ReturnService borrowReturnService;
	private final NotificationService notificationService;

	public BorrowReturnController(Borrow_ReturnService borrowReturnService, NotificationService notificationService) {
		this.borrowReturnService = borrowReturnService;
        this.notificationService = notificationService;
    }

	@PostMapping("/borrow_return_view")
	public String showBorrowReturns(Model model) {
		List<Borrow_Return> borrowReturns = borrowReturnService.getConfirmedBorrowReturns();
		model.addAttribute("borrowReturns", borrowReturns);
		return "borrow_return_view"; // Tên file Thymeleaf
	}

	@PostMapping("/borrow-confirm")
	public String confirmBorrow(@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
		Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);

		if (borrowReturn != null && "pending".equals(borrowReturn.getStatus())) {
			LocalDate now = LocalDate.now();
			borrowReturn.setStartDate(java.util.Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			borrowReturn.setStatus("borrowed");
			borrowReturnService.save(borrowReturn);

			// Lưu thông báo vào database
			Notification notification = new Notification();
			notification.setUser(borrowReturn.getUser());
			notification.setMessage("Bạn đã mượn sách " + borrowReturn.getBook().getBookName() + " thành công!");
			notification.setType("borrow_success");
			notification.setCreatedAt(LocalDateTime.now()); // Cập nhật đúng kiểu dữ liệu
			notificationService.save(notification);

			redirectAttributes.addFlashAttribute("message", "Xác nhận mượn thành công!");
		} else {
			redirectAttributes.addFlashAttribute("error", "Không thể xác nhận mượn!");
		}
		return "forward:/admin/borrow_return_view";
	}


	// Xác nhận trả sách (Chuyển từ borrowed → returned hoặc outdate)
	@PostMapping("/borrow-return")
	public String returnBook(@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
		Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);

		if (borrowReturn == null) {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn mượn sách!");
			return "forward:/admin/borrow_return_view";
		}

		if (!"borrowed".equals(borrowReturn.getStatus())) {
			redirectAttributes.addFlashAttribute("error", "Sách chưa được mượn hoặc đã trả!");
			return "forward:/admin/borrow_return_view";
		}

		LocalDate now = LocalDate.now();
		LocalDate startDate = new java.sql.Date(borrowReturn.getStartDate().getTime())
				.toLocalDate();



		long daysBorrowed = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);

		borrowReturn.setEndDate(java.util.Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));

		if (daysBorrowed > 15) {
			borrowReturn.setStatus("outdate"); // Nếu quá 15 ngày, đánh dấu quá hạn
		} else {
			borrowReturn.setStatus("returned"); // Nếu trong 15 ngày, đánh dấu đã trả
		}

		borrowReturnService.save(borrowReturn);
		redirectAttributes.addFlashAttribute("message", "Xác nhận trả sách thành công!");

		return "forward:/admin/borrow_return_view";
	}


}

package com.project.admin.controller;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.entity.Notification;
import com.project.admin.entity.User;
import com.project.admin.service.BookService;
import com.project.admin.service.Borrow_ReturnService;
import com.project.admin.service.NotificationService;
import com.project.admin.service.UserService;
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
	private final BookService bookService;
	private final UserService userService;

	public BorrowReturnController(Borrow_ReturnService borrowReturnService, NotificationService notificationService, BookService bookService, UserService userService) {
		this.borrowReturnService = borrowReturnService;
		this.notificationService = notificationService;
		this.bookService = bookService;
        this.userService = userService;
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

			// Giảm số lượng sách đi 1 và tăng số lượng mượn của sách
			Book book = borrowReturn.getBook();
			if (book.getAmount() > 0) {
				book.setAmount(book.getAmount() - 1);
				book.setBorrowCount(book.getBorrowCount() + 1); // Cập nhật số lượng mượn sách
				bookService.save(book);
			} else {
				redirectAttributes.addFlashAttribute("error", "Sách đã hết!");
				return "forward:/admin/borrow_return_view";
			}

			// Tăng số lượt mượn của người dùng
			User user = borrowReturn.getUser();
			user.setBorrowCount(user.getBorrowCount() + 1); // Cập nhật số lượt mượn của user
			userService.save(user);

			borrowReturnService.save(borrowReturn);

			// Lưu thông báo vào database
			Notification notification = new Notification();
			notification.setUser(user);
			notification.setMessage("Bạn đã mượn sách " + book.getBookName() + " thành công!");
			notification.setType("borrow_success");
			notification.setCreatedAt(LocalDateTime.now());
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
		LocalDate startDate = new java.sql.Date(borrowReturn.getStartDate().getTime()).toLocalDate();
		long daysBorrowed = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);

		borrowReturn.setEndDate(java.util.Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));

		if (daysBorrowed > 15) {
			borrowReturn.setStatus("outdate"); // Nếu quá 15 ngày, đánh dấu quá hạn
		} else {
			borrowReturn.setStatus("returned"); // Nếu trong 15 ngày, đánh dấu đã trả
		}

		borrowReturnService.save(borrowReturn);

		// Tăng số lượng sách lên 1
		Book book = borrowReturn.getBook();
		book.setAmount(book.getAmount() + 1);
		bookService.save(book);

		redirectAttributes.addFlashAttribute("message", "Xác nhận trả sách thành công!");
		return "forward:/admin/borrow_return_view";
	}



}

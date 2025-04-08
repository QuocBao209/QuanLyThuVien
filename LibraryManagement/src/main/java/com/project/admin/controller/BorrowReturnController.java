package com.project.admin.controller;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.entity.Notification;
import com.project.admin.entity.User;
import com.project.admin.service.BookService;
import com.project.admin.service.Borrow_ReturnService;
import com.project.admin.service.NotificationService;
import com.project.admin.service.UserService;
import com.project.admin.utils.AdminCodes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
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
	public String showBorrowReturns(@RequestParam("userId") Long userId, Model model) {
	    // Lấy thông tin user
	    User user = userService.findById(userId).orElse(null);
	    if (user == null) {
	        return "error";
	    }

	    // Lấy danh sách Borrow_Return của user đó
	    List<Borrow_Return> borrowReturns = borrowReturnService.findByUser_UserId(userId); 

	    borrowReturns.sort(Comparator.comparingInt(b -> {
	        switch (b.getStatus()) {
	            case "pending": return 1;
	            case "borrowed": return 2;
	            case "returned": return 3;
	            case "outdate": return 4;
	            default: return 5;
	        }
	    }));
	    
	    model.addAttribute("user", user);
	    model.addAttribute("borrowReturns", borrowReturns);
	    return "borrow_return_view";
	}
	
	// GetMapping chỗ này để hệ thống chạy method showBorrowReturns mà không bị mất tham số userId
	@GetMapping("/borrow_return_view")
	public String showBorrowReturnsGet(@RequestParam("userId") Long userId, Model model) {
	    return showBorrowReturns(userId, model);
	}

	@PostMapping("/borrow-confirm")
	public String confirmBorrow(@RequestParam("borrowId") Long borrowId, Model model) {

	    Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);
	    if (borrowReturn != null && "pending".equals(borrowReturn.getStatus())) {
			Book book = borrowReturn.getBook();
			book.setAmount(book.getAmount() - 1);
			book.setBorrowCount(book.getBorrowCount() + 1);

			bookService.save(book);

	        borrowReturn.setStartDate(new Date());
	        borrowReturn.setStatus("borrowed");
	        borrowReturnService.save(borrowReturn);

	        User user = borrowReturn.getUser();
			user.setBorrowCount(user.getBorrowCount() + 1);
			userService.save(user);

	        Notification notification = new Notification();
	        notification.setUser(user);
	        notification.setMessage("Bạn đã mượn sách " + borrowReturn.getBook().getBookName() + " thành công!");
	        notification.setType("borrow_success");
	        notification.setCreatedAt(LocalDateTime.now());
	        notificationService.save(notification);


			model.addAttribute("message", AdminCodes.getSuccessMessage("BORROW_CONFIRM_SUCCESS"));
			model.addAttribute("user", user);
	        model.addAttribute("borrowReturns", borrowReturnService.findByUser_UserId(user.getUserId()));

	        return "borrow_return_view";
	    }

		model.addAttribute("error", AdminCodes.getErrorMessage("BORROW_CONFIRM_FAILED"));

	    return "borrow_return_view";
	}



	@PostMapping("/borrow-return")
	public String returnBook(@RequestParam("borrowId") Long borrowId,
							 @RequestParam("bookCondition") String bookCondition,
							 Model model) {
		Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);


		LocalDate now = LocalDate.now();
		LocalDate startDate = new java.sql.Date(borrowReturn.getStartDate().getTime()).toLocalDate();
		long daysBorrowed = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);

		borrowReturn.setEndDate(java.util.Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));

		Book book = borrowReturn.getBook();
		User user = borrowReturn.getUser();

		String statusMessage;

		if (daysBorrowed > 15) {
			borrowReturn.setStatus("outdate");
			user.setViolationCount(user.getViolationCount() + 1);
			statusMessage = "Bạn đã trả sách " + book.getBookName() + " quá hạn (" + daysBorrowed + " ngày).";
			userService.checkAndLockUser(user.getUserId());
		} else {
			borrowReturn.setStatus("returned");
			statusMessage = "Bạn đã trả sách " + book.getBookName() + " thành công!";
		}

		if ("damaged".equals(bookCondition)) {
			book.setIsDamaged(book.getIsDamaged() + 1);
			user.setViolationCount(user.getViolationCount() + 1);
			statusMessage += " Tuy nhiên, sách bị hư hỏng hoặc mất!";
			userService.checkAndLockUser(user.getUserId());
		} else {
			book.setAmount(book.getAmount() + 1);
		}

		borrowReturnService.save(borrowReturn);
		bookService.save(book);
		userService.save(user);


		// Tạo thông báo
		Notification notification = new Notification();
		notification.setUser(user);
		notification.setMessage(statusMessage);
		notification.setType(daysBorrowed > 15 ? "return_outdate" : "return_success");
		notification.setCreatedAt(LocalDateTime.now());
		notification.setRead(false);
		notificationService.save(notification);

		model.addAttribute("successMessage", AdminCodes.getSuccessMessage("RETURN_CONFIRM_SUCCESS"));
		model.addAttribute("user", user);
		model.addAttribute("borrowReturns", borrowReturnService.findByUser_UserId(user.getUserId()));


		return "borrow_return_view";
	}


}
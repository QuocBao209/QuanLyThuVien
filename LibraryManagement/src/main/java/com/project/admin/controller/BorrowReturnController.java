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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	        return "error"; // Hoặc redirect với thông báo lỗi
	    }

	    // Lấy danh sách Borrow_Return của user đó
	    List<Borrow_Return> borrowReturns = borrowReturnService.findByUser_UserId(userId); 
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
	public String confirmBorrow(@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
	    Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);
	    if (borrowReturn != null && "pending".equals(borrowReturn.getStatus())) {
	        // Logic xác nhận mượn
	        borrowReturn.setStartDate(new Date());
	        borrowReturn.setStatus("borrowed");
	        borrowReturnService.save(borrowReturn);

	        User user = borrowReturn.getUser();
	        Notification notification = new Notification();
	        notification.setUser(user);
	        notification.setMessage("Bạn đã mượn sách " + borrowReturn.getBook().getBookName() + " thành công!");
	        notification.setType("borrow_success");
	        notification.setCreatedAt(LocalDateTime.now());
	        notificationService.save(notification);

	        redirectAttributes.addFlashAttribute("message", "Xác nhận mượn thành công!");
	    }
	    
	    // Đổi cú pháp đường dẫn
	    return "redirect:/admin/borrow_return_view?userId=" + borrowReturn.getUser().getUserId();
	}

	// Xác nhận trả sách (Chuyển từ borrowed → returned hoặc outdate)
	@PostMapping("/borrow-return")
	public String returnBook(@RequestParam("borrowId") Long borrowId, RedirectAttributes redirectAttributes) {
	    Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);

	    if (borrowReturn == null) {
	        redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn mượn sách!");
	        return "redirect:/admin/borrow_return_view";
	    }

	    if (!"borrowed".equals(borrowReturn.getStatus())) {
	        redirectAttributes.addFlashAttribute("error", "Sách chưa được mượn hoặc đã trả!");
	        return "redirect:/admin/borrow_return_view?userId=" + borrowReturn.getUser().getUserId();
	    }

	    LocalDate now = LocalDate.now();
	    LocalDate startDate = new java.sql.Date(borrowReturn.getStartDate().getTime()).toLocalDate();
	    long daysBorrowed = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);

	    borrowReturn.setEndDate(java.util.Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));

	    String statusMessage;
	    if (daysBorrowed > 15) {
	        borrowReturn.setStatus("outdate");
	        statusMessage = "Bạn đã trả sách " + borrowReturn.getBook().getBookName() + " quá hạn (" + daysBorrowed + " ngày).";
	    } else {
	        borrowReturn.setStatus("returned");
	        statusMessage = "Bạn đã trả sách " + borrowReturn.getBook().getBookName() + " thành công!";
	    }

	    borrowReturnService.save(borrowReturn);

	    // Tăng số lượng sách lên 1
	    Book book = borrowReturn.getBook();
	    book.setAmount(book.getAmount() + 1);
	    bookService.save(book);

	    // Tạo và lưu thông báo
	    User user = borrowReturn.getUser();
	    Notification notification = new Notification();
	    notification.setUser(user);
	    notification.setMessage(statusMessage);
	    notification.setType(daysBorrowed > 15 ? "return_outdate" : "return_success");
	    notification.setCreatedAt(LocalDateTime.now());
	    notification.setRead(false); // Thông báo mới, chưa đọc
	    notificationService.save(notification);

	    redirectAttributes.addFlashAttribute("message", "Xác nhận trả sách thành công!");
	    
	    // Đổi cú pháp đường dẫn
	    return "redirect:/admin/borrow_return_view?userId=" + borrowReturn.getUser().getUserId();
	}
}
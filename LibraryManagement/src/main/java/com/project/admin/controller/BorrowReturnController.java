package com.project.admin.controller;

import com.project.admin.entity.Book;
import com.project.admin.entity.Borrow_Return;
import com.project.admin.entity.Notification;
import com.project.admin.entity.User;
import com.project.admin.service.*;
import com.project.admin.utils.AdminCodes;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    @Autowired
    private EmailService emailService;
    public BorrowReturnController(Borrow_ReturnService borrowReturnService, NotificationService notificationService, BookService bookService, UserService userService) {
        this.borrowReturnService = borrowReturnService;
        this.notificationService = notificationService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @PostMapping("/borrow_return_view")
    public String showBorrowReturns(@RequestParam("userId") Long userId, Model model) {
        User user = userService.findById(userId).orElse(null);

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
            return showBorrowReturns(user.getUserId(), model);
        }

        return showBorrowReturns(borrowReturn.getUser().getUserId(), model);
    }

    @PostMapping("/borrow-return")
    public String returnBook(@RequestParam("borrowId") Long borrowId,
                             @RequestParam("bookCondition") String bookCondition,
                             Model model) {
        Borrow_Return borrowReturn = borrowReturnService.findById(borrowId);
        if (borrowReturn == null) {
            model.addAttribute("error", AdminCodes.getErrorMessage("BORROW_NOT_FOUND"));
            return "redirect:/admin/user-list";
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate = new java.sql.Date(borrowReturn.getStartDate().getTime()).toLocalDate();
        long daysBorrowed = ChronoUnit.DAYS.between(startDate, now);

        borrowReturn.setEndDate(java.util.Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        Book book = borrowReturn.getBook();
        User user = borrowReturn.getUser();

        String statusMessage;

        boolean violated = false;

        // Mượn tối đa 2 tuần
        if (daysBorrowed > 14) {
            borrowReturn.setStatus("outdate");
            user.setViolationCount(user.getViolationCount() + 1);
            violated = true;
            statusMessage = "Bạn đã trả sách " + book.getBookName() + " quá hạn (" + daysBorrowed + " ngày).";
        } else {
            borrowReturn.setStatus("returned");
            statusMessage = "Bạn đã trả sách " + book.getBookName() + " thành công!";
        }

        if ("damaged".equals(bookCondition)) {
            book.setIsDamaged(book.getIsDamaged() + 1);
            user.setViolationCount(user.getViolationCount() + 1);
            violated = true;
            statusMessage += " Tuy nhiên, sách bị hư hỏng hoặc mất!";
        } else {
            book.setAmount(book.getAmount() + 1);
        }

        borrowReturnService.save(borrowReturn);
        bookService.save(book);
        userService.save(user);

        if (violated) {
            userService.checkAndLockUser(user.getUserId());

            if (user.getViolationCount() > 3) {
                try {
                    emailService.sendEmail(
                            user.getEmail(),
                            "Tài khoản của bạn đã bị khóa",
                            "<p>Chào " + user.getName() + ",</p>" +
                                    "<p>Tài khoản của bạn đã bị khóa do vi phạm quy định quá số lần cho phép.</p>" +
                                    "<p>Vui lòng liên hệ quản trị viên để biết thêm chi tiết.</p>"
                    );
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(statusMessage);
        notification.setType(daysBorrowed > 14 ? "return_outdate" : "return_success");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationService.save(notification);

        model.addAttribute("successMessage", AdminCodes.getSuccessMessage("RETURN_CONFIRM_SUCCESS"));
        return showBorrowReturns(user.getUserId(), model);
    }
}
package com.project.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import com.project.demo.service.BookService;
import com.project.demo.service.NotificationService;
import com.project.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class BestBookController {
	
	@Autowired private BookService bookService;
	@Autowired private NotificationService notificationService;
	@Autowired private UserService userService;
	 
	 public BestBookController(BookService bookService, NotificationService notificationService, UserService userService) {
	    this.bookService = bookService;
	    this.notificationService = notificationService;
	    this.userService = userService;
	 }
	 
	 @GetMapping("/best-book")
	 public ModelAndView bestBookPage(@RequestParam(defaultValue = "0") int page,
	                                  @RequestParam(defaultValue = "5") int size,
	                                  HttpSession session,
	                                  HttpServletRequest request) {
	     ModelAndView model = new ModelAndView("bestBook");

	     // Lấy danh sách sách
	     List<Book> topBooks = bookService.getTop3Books();
	     Page<Book> pagedBooks = bookService.getPagedBooksSortedByBorrowCountExcludingTop3(page, size);

	     // Kiểm tra session user
	     String username = (String) session.getAttribute("user");
	     if (username != null) {
	         Optional<User> userOptional = userService.getUserByUsername(username);
	         if (userOptional.isPresent()) {
	             User user = userOptional.get();

	             // Lấy danh sách thông báo
	             List<Notification> notifications = notificationService.getNotificationsByUsername(username);
	             
	             // Sắp xếp thông báo từ mới nhất đến cũ nhất
	             notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

	             // Đếm số thông báo chưa đọc
	             long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

	             // Gửi dữ liệu đến view
	             model.addObject("user", user);
	             model.addObject("notifications", notifications);
	             model.addObject("unreadCount", unreadCount);
	         }
	     }

	     model.addObject("topBooks", topBooks);
	     model.addObject("bookPage", pagedBooks);
	     model.addObject("currentPage", page);
	     model.addObject("currentUrl", request.getRequestURI()); 

	     return model; 
	 }

}
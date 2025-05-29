package com.project.demo.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.entity.Category;
import com.project.demo.entity.Notification;
import com.project.demo.entity.User;
import com.project.demo.service.BookService;
import com.project.demo.service.CategoryService;
import com.project.demo.service.NotificationService;
import com.project.demo.service.UserService;
import com.project.demo.utils.UserCodes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")
public class LatestBookController {
	
	@Autowired private BookService bookService;
	@Autowired private CategoryService categoryService;
	@Autowired private NotificationService notificationService;
	@Autowired private UserService userService;
	 
	 public LatestBookController(BookService bookService, CategoryService categoryService, 
			 					 NotificationService notificationService, UserService userService) {
	    this.bookService = bookService;
	    this.categoryService = categoryService;
	    this.notificationService = notificationService;
	    this.userService = userService;
	 }
	@GetMapping("/latest-book")
    public ModelAndView latestBookPage(
    		@RequestParam(value = "categoryName", required = false) Set<String> categoryNames,
	        @RequestParam(value = "timeFilter", required = false) String timeRange,
    		@RequestParam(defaultValue = "0") int page,
    		@RequestParam(defaultValue = "10") int size,
    		@RequestParam(value = "keyword", required = false) String keyword,
    		HttpSession session,
            HttpServletRequest request){
        ModelAndView model = new ModelAndView("latestBook");
        
        List<Category> categories = categoryService.getAllCategories();
        Page<Book> bookPage = bookService.getLatestBooks(categoryNames, keyword, page, size);
        
     // Kiểm tra nếu không có sách
        if (bookPage == null || bookPage.getContent().isEmpty()) {
        	model.addObject("noData", UserCodes.getErrorMessage("BOOK_NOT_FOUND"));
        }
        
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
        
        // Thêm requestURI vào model
	    model.addObject("currentUrl", request.getRequestURI());

	    // Thêm dữ liệu vào Model
	    model.addObject("bookPage", bookPage);
	    model.addObject("currentPage", page);
	    model.addObject("categories", categories);
	    model.addObject("categoryNames", categoryNames);
	    model.addObject("selectedTimeRange", timeRange);
        
        return model;
    }
}

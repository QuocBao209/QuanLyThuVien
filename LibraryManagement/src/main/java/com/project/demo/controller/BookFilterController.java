package com.project.demo.controller;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/home")
public class BookFilterController {

	@Autowired private BookService bookService;
	@Autowired private CategoryService categoryService;
	@Autowired private NotificationService notificationService;
	@Autowired private UserService userService;

	public BookFilterController(BookService bookService, CategoryService categoryService, 
								NotificationService notificationService, UserService userService) {
		this.bookService = bookService;
		this.categoryService = categoryService;
		this.notificationService = notificationService;
		this.userService = userService;
	}
	
	@GetMapping("/book-filter")
    public ModelAndView bookFilterPage(
            @RequestParam(value = "categoryName", required = false) Set<String> categoryNames,
            @RequestParam(value = "timeFilter", required = false) String timeRange,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session,
            HttpServletRequest request) {

        ModelAndView mav = new ModelAndView("bookFilter");

        // Lấy danh sách danh mục sách
        List<Category> categories = categoryService.getAllCategories();

        // Lọc sách theo danh mục, thời gian, và từ khóa
        Page<Book> bookPage = bookService.filterBooks(categoryNames, timeRange, keyword, page, size);
        
        // Kiểm tra nếu không có sách
        if (bookPage == null || bookPage.getContent().isEmpty()) {
            mav.addObject("noData", UserCodes.getErrorMessage("BOOK_NOT_FOUND"));
        }

        // Kiểm tra session user
        String username = (String) session.getAttribute("user");
        if (username != null) {
            Optional<User> userOptional = userService.getUserByUsername(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Lấy danh sách thông báo của user
                List<Notification> notifications = notificationService.getNotificationsByUsername(username);
                
                // Sắp xếp thông báo từ mới nhất đến cũ nhất
                notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));

                // Đếm số lượng thông báo chưa đọc
                long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

                // Gửi dữ liệu đến view
                mav.addObject("user", user);
                mav.addObject("notifications", notifications);
                mav.addObject("unreadCount", unreadCount);
            }
        }

        // Thêm requestURI vào model
        mav.addObject("currentUrl", request.getRequestURI());

        // Thêm dữ liệu vào Model
        mav.addObject("bookPage", bookPage);
        mav.addObject("currentPage", page);
        mav.addObject("categories", categories);
        mav.addObject("categoryNames", categoryNames);
        mav.addObject("selectedTimeRange", timeRange);
        mav.addObject("keyword", keyword);

        return mav;
    }
}

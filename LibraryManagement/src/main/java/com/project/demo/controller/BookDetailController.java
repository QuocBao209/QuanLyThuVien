package com.project.demo.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.entity.Borrow_Return;
import com.project.demo.entity.User;
import com.project.demo.service.BookService;
import com.project.demo.service.Borrow_ReturnService;
import com.project.demo.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/home")

public class BookDetailController {
	
	@Autowired private BookService bookService;
	
	@Autowired private UserService userService;
	
	@Autowired private Borrow_ReturnService borrowService;
	
	public BookDetailController(BookService bookService) {
	    this.bookService = bookService;
	 }
	
	@GetMapping("/book-detail/{id}") 
	public ModelAndView bookDetailForm(@PathVariable("id") Long bookId,
									   @RequestParam(defaultValue = "0") int page) {
		ModelAndView mav = new ModelAndView("bookDetail");
		Book book = bookService.getBookById(bookId);
		mav.addObject("book", book);
		
		// Lấy danh sách đề cử theo tác giả (mỗi trang 4 sách)
		Page<Book> recBooks = bookService.getBooksByAuthors(book.getAuthors(), bookId, page, 4);
		mav.addObject("recBooks", recBooks);
		
		return mav;
	}
	
	// Xử lý nút Mượn sách (Bảo) ( tương tác giữa account và nút "Mượn sách")
	@PostMapping("/submit-borrow/{id}")
	public String submitBorrow(@PathVariable Long id, HttpSession session) {
	    String username = (String) session.getAttribute("user");
	    
	    if (username == null) {
	        return "redirect:/login"; // Nếu chưa đăng nhập, chuyển hướng về trang login
	    }
	    
	    Optional<User> userOptional = userService.getUserByUsername(username);
	    Optional<Book> bookOptional = bookService.getOptionalBookById(id);
	    
	    if (userOptional.isPresent() && bookOptional.isPresent()) {
	        Borrow_Return borrow = new Borrow_Return();
	        borrow.setUser(userOptional.get());
	        borrow.setBook(bookOptional.get());
	        
	        // Đặt tất cả các trường ngày là null
	        borrow.setUserConfirmDate(null);
	        borrow.setStartDate(null);
	        borrow.setEndDate(null);
	        
	        borrow.setStatus("pending"); // Trạng thái ban đầu là "Đang chờ"
	        borrow.setRenewCount(0);
	        
	        borrowService.saveBorrow(borrow);
	    }
	    
	    return "redirect:/home/account"; // Sau khi mượn, chuyển hướng về trang tài khoản
	}

}

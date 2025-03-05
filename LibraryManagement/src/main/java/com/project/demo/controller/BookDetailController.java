package com.project.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.service.BookService;

@Controller
@RequestMapping("/home")

public class BookDetailController {
	
	@Autowired private BookService bookService;
	
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

}

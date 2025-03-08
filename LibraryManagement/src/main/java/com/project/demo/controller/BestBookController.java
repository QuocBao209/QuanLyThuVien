package com.project.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.service.BookService;

@Controller
@RequestMapping("/home")
public class BestBookController {
	
	@Autowired private BookService bookService;
	 
	 public BestBookController(BookService bookService) {
	    this.bookService = bookService;
	 }
	@GetMapping("/best-book")
	public ModelAndView bestBookPage(@RequestParam(defaultValue = "0") int page,
	                           @RequestParam(defaultValue = "5") int size) {
		ModelAndView model = new ModelAndView("bestBook");
	    List<Book> topBooks = bookService.getTop3Books();
	    Page<Book> pagedBooks = bookService.getPagedBooksSortedByBorrowCountExcludingTop3(page, size);

	    model.addObject("topBooks", topBooks);
	    model.addObject("bookPage", pagedBooks);
	    model.addObject("currentPage", page);

	    return model; // Trả về view bestBooks.html
	}
}

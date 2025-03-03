package com.project.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.service.BookService;

@Controller
@RequestMapping("/home")
public class LatestBookController {
	
	@Autowired private BookService bookService;
	 
	 public LatestBookController(BookService bookService) {
	    this.bookService = bookService;
	 }
	@GetMapping("/latest-book")
	public ModelAndView latestBookPage() {
		ModelAndView latestbook = new ModelAndView("latestBook");
		List<Book> books = bookService.getBooks();
		latestbook.addObject("books", books);
		return latestbook;
	}
}

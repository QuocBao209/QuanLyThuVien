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
public class BestBookController {
	
	@Autowired private BookService bookService;
	 
	 public BestBookController(BookService bookService) {
	    this.bookService = bookService;
	 }
	@GetMapping("/best-book")
	public ModelAndView bestBookPage() {
		ModelAndView bestBook = new ModelAndView("bestBook");
		List<Book> books = bookService.getBooks();
		bestBook.addObject("books", books);
		return bestBook;
	}
}

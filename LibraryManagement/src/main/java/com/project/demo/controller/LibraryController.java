package com.project.demo.controller;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.project.demo.entity.Book;
import com.project.demo.service.BookService;

@Controller
@RequestMapping("/home")
public class LibraryController {
	
	 @Autowired private BookService bookService;
	 
	 public LibraryController(BookService bookService) {
	    this.bookService = bookService;
	 }

	@GetMapping("/library-page")
	public ModelAndView libraryPage() {
		ModelAndView mav = new ModelAndView("libraryPage");
		List<Book> books = bookService.getBooks();
		mav.addObject("books", books);
		return mav;
	}
}

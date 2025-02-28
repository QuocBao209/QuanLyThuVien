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

	@GetMapping("/libraryPage")
	public ModelAndView libraryPage() {
		ModelAndView mav = new ModelAndView("libraryPage");
		List<Book> books = bookService.getBooks().stream().map(book -> {
            // Chuyển đổi ảnh thành Base64
            if (book.getBookImage() != null) {
                String base64Image = Base64.getEncoder().encodeToString(book.getBookImage());
                book.setBase64Image(base64Image); // Gán vào thuộc tính mới
            }
            return book;
        }).collect(Collectors.toList());
		mav.addObject("books", books);
		return mav;
	}
}

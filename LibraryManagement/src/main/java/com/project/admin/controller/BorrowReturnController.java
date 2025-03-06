package com.project.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.admin.entity.Borrow_Return;
import com.project.admin.repository.BookRepository;
import com.project.admin.repository.Borrow_ReturnRepository;

@Controller 
@RequestMapping("/admin")
public class BorrowReturnController {
	
	 @Autowired
	 private Borrow_ReturnRepository borrowReturnRepository;

	 @Autowired
	 private BookRepository bookRepository;
	
	@PostMapping("/borrow-return-view")
	public String historyTable(Model model) {
		List<Borrow_Return> borrowedBooks = borrowReturnRepository.findAll();
		model.addAttribute("borrowedBooks", borrowedBooks);
		return "borrow_return_view";
	}
}

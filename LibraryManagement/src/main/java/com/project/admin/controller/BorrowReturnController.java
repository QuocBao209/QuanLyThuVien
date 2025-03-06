package com.project.admin.controller;

import com.project.admin.entity.Borrow_Return;
import com.project.admin.service.Borrow_ReturnService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class BorrowReturnController {
	private final Borrow_ReturnService borrowReturnService;

	public BorrowReturnController(Borrow_ReturnService borrowReturnService) {
		this.borrowReturnService = borrowReturnService;
	}

	@PostMapping("/borrow_return_view") // Đổi từ @PostMapping sang @GetMapping
	public String showBorrowReturns(Model model) {
		List<Borrow_Return> borrowReturns = borrowReturnService.getBorrowReturns();
		model.addAttribute("borrowReturns", borrowReturns);
		return "borrow_return_view"; // Tên file Thymeleaf
	}
}

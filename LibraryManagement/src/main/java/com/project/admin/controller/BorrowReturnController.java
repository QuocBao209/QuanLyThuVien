//package com.project.demo.controller;
//
//import java.util.List;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import com.project.admin.entity.Borrow_Return;
//import com.project.admin.service.Borrow_ReturnService;
//
//import ch.qos.logback.core.model.Model;
//
//@Controller 
//@RequestMapping("/admin")
//public class BorrowReturnController {
//	@GetMapping("/borrow-return-list")
//	public String borrowReturnForm(Model model) {
//		List<Borrow_Return> borrowReturnList = Borrow_ReturnService.getBorrowReturns();
//		model.addAttribute("borrowReturnList", borrowReturnList);
//		return "borrow_return_view";
//	}
//}

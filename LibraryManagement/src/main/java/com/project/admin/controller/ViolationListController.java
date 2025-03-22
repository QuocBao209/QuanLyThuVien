package com.project.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.project.admin.service.UserService;

@Controller
@RequestMapping("/admin")
public class ViolationListController {
	@Autowired private UserService userService;
	
	@PostMapping("/violation-list")
	public ModelAndView violationListForm(@RequestParam(value = "keyword", required = false) String keyword) {
	    ModelAndView modelAndView = new ModelAndView("violation_list");

	    if (keyword == null || keyword.isEmpty()) {
	        modelAndView.addObject("users", userService.getUsersWithViolations());
	    } else {
	        modelAndView.addObject("users", userService.searchUsersWithViolations(keyword));
	    }

	    return modelAndView;
	}

}

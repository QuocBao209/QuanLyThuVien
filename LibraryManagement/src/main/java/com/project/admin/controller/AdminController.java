package com.project.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminController {
	@PostMapping("/admin")
	public ModelAndView showAdminPage() {
		return new ModelAndView("admin");
	}
}

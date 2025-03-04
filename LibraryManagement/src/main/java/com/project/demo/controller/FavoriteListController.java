package com.project.demo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.project.demo.entity.Book;
import com.project.demo.repository.BookRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class FavoriteListController {
	
	@Autowired private BookRepository bookRepository;
	
	@PostMapping("/home/library-page/add/{id}")
	public String addToList(@PathVariable("id") Long bookId, HttpServletResponse response, 
							@CookieValue(name = "cart", defaultValue = "") String cart) {
		Set<String> bookIds = new HashSet<>(Arrays.asList(cart.split(",")));
		bookIds.add(bookId.toString());
		
		String newCart = String.join("-", bookIds);
		Cookie cookie = new Cookie("cart", newCart);
		cookie.setPath("/");
		response.addCookie(cookie);
		
		return "redirect:/home/library-page";
	}
	
	@GetMapping("/home/favorite-list")
	public String favoriteView(Model model, @CookieValue (name = "cart", defaultValue = "") String cart)  {
		if (cart.isEmpty()) {
			model.addAttribute("borrowedBooks", new ArrayList<>());
		} else {
			List<Long> bookIds = Arrays.stream(cart.split("-")) 
		            .filter(s -> !s.isEmpty()) // Bỏ qua phần tử rỗng (do "--")
		            .map(s -> {
		                try {
		                    return Long.parseLong(s);
		                } catch (NumberFormatException e) {
		                    return null; // Bỏ qua phần tử không hợp lệ
		                }
		            })
		            .filter(Objects::nonNull) // Loại bỏ phần tử null
		            .collect(Collectors.toList());
			List<Book> books = bookRepository.findAllById(bookIds);
			model.addAttribute("borrowedBooks", books);
		}
		
		return "favoriteList";
	}
}

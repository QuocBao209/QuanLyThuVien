package com.project.demo.service;

import com.project.demo.entity.Author;
import com.project.demo.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {
    @Autowired private AuthorRepository authorRepository;

    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    public void transferData(List<Author> authors) {
        authorRepository.saveAll(authors);
    }

    public Author findByName(String authorName) {
        return authorRepository.findByName(authorName).orElse(null);  // Kiểm tra nếu tác giả đã tồn tại
    }

    public Author saveAuthor(Author author) {
        return authorRepository.save(author);  // Lưu tác giả mới
    }
}

package com.project.demo.service;

import com.project.demo.entity.Author;
import com.project.demo.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    // Lấy danh sách tất cả tác giả
    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    // Lưu danh sách tác giả
    public void transferData(List<Author> authors) {
        if (authors != null && !authors.isEmpty()) {
            authorRepository.saveAll(authors);
        }
    }


    // Tìm tác giả theo ID
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }


}

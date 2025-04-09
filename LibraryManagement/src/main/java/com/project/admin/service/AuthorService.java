package com.project.admin.service;

import com.project.admin.entity.Author;
import com.project.admin.repository.AuthorRepository;
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

    // Tìm tác giả theo tên (không phân biệt hoa thường)
    public Optional<Author> findByName(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            return Optional.empty();
        }
        return authorRepository.findByAuthorName(authorName.trim());
    }

    // Tìm tác giả theo ID
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }

    // Lưu tác giả mới (dùng khi chắc chắn chưa có)
    public Author saveAuthor(Author author) {
        if (author == null || author.getAuthorName() == null || author.getAuthorName().isBlank()) {
            throw new IllegalArgumentException("Tên tác giả không được để trống!");
        }
        return authorRepository.save(author);
    }

}

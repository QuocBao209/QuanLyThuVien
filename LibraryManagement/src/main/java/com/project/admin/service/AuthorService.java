package com.project.admin.service;

import com.project.admin.entity.Author;
import com.project.admin.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    public void transferData(List<Author> authors) {
        if (authors != null && !authors.isEmpty()) {
            authorRepository.saveAll(authors);
        }
    }

    public Author findByName(String authorName) {
        return authorRepository.findByAuthorName(authorName).orElse(null);  // Đổi thành findByAuthorName
    }

    public Author saveAuthor(Author author) {
        if (author == null || author.getAuthorName() == null || author.getAuthorName().isBlank()) {
            throw new IllegalArgumentException("Tên tác giả không được để trống!");
        }

        // Kiểm tra xem tác giả đã tồn tại chưa
        Author existingAuthor = findByName(author.getAuthorName());
        if (existingAuthor != null) {
            return existingAuthor; // Nếu có rồi, trả về luôn
        }

        return authorRepository.save(author); // Nếu chưa, lưu mới
    }
}

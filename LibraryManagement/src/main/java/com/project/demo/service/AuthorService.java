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
}

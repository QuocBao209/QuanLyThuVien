package com.project.demo.service;

import com.project.demo.entity.Category;
import com.project.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired private CategoryRepository categoryRepository;

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public void transferData(List<Category> categories) {
        categoryRepository.saveAll(categories);
    }
}

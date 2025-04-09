package com.project.demo.service;

import com.project.demo.entity.Category;
import com.project.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy tất cả thể loại từ cơ sở dữ liệu
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    // Lưu danh sách thể loại vào cơ sở dữ liệu
    public void transferData(List<Category> categories) {
        if (categories != null && !categories.isEmpty()) {
            categoryRepository.saveAll(categories);
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Tìm thể loại theo tên
    public Category findByName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName).orElse(null);
    }

}

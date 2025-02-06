package com.project.demo.service;

import com.project.demo.entity.Category;
import com.project.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy tất cả thể loại từ cơ sở dữ liệu
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    // Lưu tất cả thể loại vào cơ sở dữ liệu
    public void transferData(List<Category> categories) {
        categoryRepository.saveAll(categories);
    }

    // Kiểm tra xem thể loại đã tồn tại trong cơ sở dữ liệu chưa
    public Category findByName(String categoryName) {
        return categoryRepository.findByName(categoryName).orElse(null); // Giả sử có phương thức này trong repository
    }

    // Lưu thể loại vào cơ sở dữ liệu
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }
}

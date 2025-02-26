package com.project.admin.service;

import com.project.admin.entity.Category;
import com.project.admin.repository.CategoryRepository;
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
        return categoryRepository.findByCategoryName(categoryName).orElse(null); // Đổi thành findByCategoryName
    }

    // Lưu thể loại vào cơ sở dữ liệu, tránh trùng lặp
    public Category saveCategory(Category category) {
        if (category == null || category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Tên thể loại không được để trống!");
        }

        // Kiểm tra xem thể loại đã tồn tại chưa
        Category existingCategory = findByName(category.getCategoryName());
        return Objects.requireNonNullElseGet(existingCategory, () -> categoryRepository.save(category)); // Nếu có rồi, trả về luôn

    }
}

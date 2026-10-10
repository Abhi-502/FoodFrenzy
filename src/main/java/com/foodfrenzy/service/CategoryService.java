package com.foodfrenzy.service;

import com.foodfrenzy.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllActiveCategories();
    List<Category> getAllCategories();
    Category getById(Long id);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
    long countCategories();
}

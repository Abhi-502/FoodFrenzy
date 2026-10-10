package com.foodfrenzy.service.impl;

import com.foodfrenzy.entity.Category;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.repository.CategoryRepository;
import com.foodfrenzy.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category updated) {
        Category category = getById(id);
        category.setName(updated.getName());
        category.setDescription(updated.getDescription());
        category.setImageUrl(updated.getImageUrl());
        category.setIconClass(updated.getIconClass());
        category.setDisplayOrder(updated.getDisplayOrder());
        category.setActive(updated.isActive());
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getById(id);
        // Soft delete category by setting active = false if has products, or delete
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            category.setActive(false);
            categoryRepository.save(category);
        } else {
            categoryRepository.delete(category);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countCategories() {
        return categoryRepository.count();
    }
}

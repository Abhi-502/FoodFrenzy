package com.foodfrenzy.service.impl;

import com.foodfrenzy.dto.ProductDto;
import com.foodfrenzy.entity.Category;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.repository.CategoryRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(Long categoryId, String keyword, String sortBy, Pageable pageable) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        if ("price_asc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("name_desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "name");
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        boolean hasCategory = (categoryId != null && categoryId > 0);

        if (hasCategory && hasKeyword) {
            return productRepository.searchActiveProductsByCategory(categoryId, keyword.trim(), sortedPageable);
        } else if (hasCategory) {
            return productRepository.findByCategoryIdAndActiveTrue(categoryId, sortedPageable);
        } else if (hasKeyword) {
            return productRepository.searchActiveProducts(keyword.trim(), sortedPageable);
        } else {
            return productRepository.findByActiveTrue(sortedPageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Override
    public Product createProduct(ProductDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());
        product.setVegetarian(dto.isVegetarian());
        if (dto.getPreparationTimeMinutes() != null) {
            product.setPreparationTimeMinutes(dto.getPreparationTimeMinutes());
        }

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, ProductDto dto) {
        Product product = getById(id);
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());
        product.setVegetarian(dto.isVegetarian());
        if (dto.getPreparationTimeMinutes() != null) {
            product.setPreparationTimeMinutes(dto.getPreparationTimeMinutes());
        }

        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getById(id);
        // Soft delete / deactivate to maintain order history integrity
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public void toggleProductStatus(Long id) {
        Product product = getById(id);
        product.setActive(!product.isActive());
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getAllProductsAdmin(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productRepository.searchAllProductsAdmin(keyword.trim(), pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStockProducts(int threshold) {
        return productRepository.countByStockQuantityLessThanEqual(threshold);
    }
}

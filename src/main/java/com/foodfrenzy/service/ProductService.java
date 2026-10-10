package com.foodfrenzy.service;

import com.foodfrenzy.dto.ProductDto;
import com.foodfrenzy.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<Product> getActiveProducts(Pageable pageable);
    List<Product> getFeaturedProducts();
    Page<Product> searchProducts(Long categoryId, String keyword, String sortBy, Pageable pageable);
    Product getById(Long id);
    Product createProduct(ProductDto dto);
    Product updateProduct(Long id, ProductDto dto);
    void deleteProduct(Long id);
    void toggleProductStatus(Long id);
    Page<Product> getAllProductsAdmin(String keyword, Pageable pageable);
    List<Product> getLowStockProducts(int threshold);
    long countProducts();
    long countLowStockProducts(int threshold);
}

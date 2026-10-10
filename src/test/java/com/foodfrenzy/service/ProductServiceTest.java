package com.foodfrenzy.service;

import com.foodfrenzy.dto.ProductDto;
import com.foodfrenzy.entity.Category;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.repository.CategoryRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category("Burgers", "Juicy burgers", "burger.jpg", "bi-egg-fried", 1);
        testCategory.setId(1L);

        testProduct = new Product("Smash Burger", "Double beef", testCategory, new BigDecimal("12.99"), "smash.jpg", 20);
        testProduct.setId(10L);
    }

    @Test
    @DisplayName("Should return product when found by ID")
    void testGetByIdSuccess() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));

        Product found = productService.getById(10L);

        assertNotNull(found);
        assertEquals("Smash Burger", found.getName());
        assertEquals(new BigDecimal("12.99"), found.getPrice());
        verify(productRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void testGetByIdNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create new product successfully")
    void testCreateProduct() {
        ProductDto dto = new ProductDto();
        dto.setName("Crispy Chicken");
        dto.setDescription("Spicy chicken burger");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("11.50"));
        dto.setStockQuantity(15);
        dto.setImageUrl("chicken.jpg");
        dto.setActive(true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(11L);
            return p;
        });

        Product created = productService.createProduct(dto);

        assertNotNull(created);
        assertEquals(11L, created.getId());
        assertEquals("Crispy Chicken", created.getName());
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return low stock products")
    void testGetLowStockProducts() {
        when(productRepository.findLowStockProducts(5)).thenReturn(List.of(testProduct));

        List<Product> lowStock = productService.getLowStockProducts(5);

        assertEquals(1, lowStock.size());
        assertEquals("Smash Burger", lowStock.get(0).getName());
        verify(productRepository, times(1)).findLowStockProducts(5);
    }

    @Test
    @DisplayName("Should deactivate product upon delete (soft-delete)")
    void testDeleteProductSoftDelete() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.deleteProduct(10L);

        assertFalse(testProduct.isActive());
        verify(productRepository, times(1)).save(testProduct);
    }
}

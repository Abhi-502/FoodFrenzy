package com.foodfrenzy.controller;

import com.foodfrenzy.entity.Category;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.service.CartService;
import com.foodfrenzy.service.CategoryService;
import com.foodfrenzy.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("GET /menu should return 200 OK and render menu template with dishes")
    void testGetMenuPage() throws Exception {
        Category cat = new Category("Burgers", "Tasty", "b.jpg", "bi-egg", 1);
        cat.setId(1L);
        Product prod = new Product("Smash Burger", "Beef", cat, new BigDecimal("12.99"), "burger.jpg", 10);
        prod.setId(5L);

        when(categoryService.getAllActiveCategories()).thenReturn(List.of(cat));
        when(productService.searchProducts(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(prod)));

        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("menu"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("GET /products/{id} should return 200 OK and render product-detail")
    void testGetProductDetailPage() throws Exception {
        Category cat = new Category("Burgers", "Tasty", "b.jpg", "bi-egg", 1);
        cat.setId(1L);
        Product prod = new Product("Smash Burger", "Beef", cat, new BigDecimal("12.99"), "burger.jpg", 10);
        prod.setId(5L);

        when(productService.getById(5L)).thenReturn(prod);

        mockMvc.perform(get("/products/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("product-detail"))
                .andExpect(model().attributeExists("product"));
    }
}

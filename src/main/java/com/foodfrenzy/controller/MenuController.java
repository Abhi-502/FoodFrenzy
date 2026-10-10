package com.foodfrenzy.controller;

import com.foodfrenzy.entity.Product;
import com.foodfrenzy.service.CategoryService;
import com.foodfrenzy.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MenuController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public MenuController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/menu")
    public String menu(@RequestParam(name = "category", required = false) Long categoryId,
                       @RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "sort", required = false, defaultValue = "name") String sortBy,
                       @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                       Model model) {

        int pageSize = 12;
        Page<Product> productPage = productService.searchProducts(categoryId, keyword, sortBy, PageRequest.of(page, pageSize));

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);

        return "menu";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        return "product-detail";
    }
}

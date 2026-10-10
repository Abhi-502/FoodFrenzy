package com.foodfrenzy.controller.admin;

import com.foodfrenzy.dto.ProductDto;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.service.CategoryService;
import com.foodfrenzy.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(@RequestParam(name = "keyword", required = false) String keyword,
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               Model model) {
        Page<Product> productPage = productService.getAllProductsAdmin(keyword, PageRequest.of(page, 10));
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        return "admin/products";
    }

    @GetMapping("/new")
    public String newProductForm(Model model) {
        if (!model.containsAttribute("product")) {
            model.addAttribute("product", new ProductDto());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isNew", true);
        return "admin/product-form";
    }

    @PostMapping
    public String createProduct(@Valid @ModelAttribute("product") ProductDto productDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isNew", true);
            return "admin/product-form";
        }

        productService.createProduct(productDto);
        redirectAttributes.addFlashAttribute("successMessage", "Food product created successfully!");
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategory().getId());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());
        dto.setVegetarian(product.isVegetarian());
        dto.setPreparationTimeMinutes(product.getPreparationTimeMinutes());

        model.addAttribute("product", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isNew", false);
        return "admin/product-form";
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("product") ProductDto productDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isNew", false);
            return "admin/product-form";
        }

        productService.updateProduct(id, productDto);
        redirectAttributes.addFlashAttribute("successMessage", "Food product updated successfully!");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleProductStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.toggleProductStatus(id);
        redirectAttributes.addFlashAttribute("infoMessage", "Product visibility updated.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.deleteProduct(id);
        redirectAttributes.addFlashAttribute("infoMessage", "Product deactivated successfully.");
        return "redirect:/admin/products";
    }
}

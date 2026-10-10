package com.foodfrenzy.controller.admin;

import com.foodfrenzy.dto.InventoryAdjustmentDto;
import com.foodfrenzy.entity.InventoryTransaction;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.security.CustomUserDetails;
import com.foodfrenzy.service.InventoryService;
import com.foodfrenzy.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    public AdminInventoryController(ProductService productService, InventoryService inventoryService) {
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String inventoryOverview(@RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "tab", defaultValue = "stock") String tab,
                                    Model model) {
        Page<Product> productPage = productService.getAllProductsAdmin(keyword, PageRequest.of(page, 15));
        Page<InventoryTransaction> transactionsPage = inventoryService.getAllTransactions(PageRequest.of(page, 15));

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("transactions", transactionsPage.getContent());
        model.addAttribute("txTotalPages", transactionsPage.getTotalPages());
        model.addAttribute("lowStockCount", productService.countLowStockProducts(5));
        model.addAttribute("keyword", keyword);
        model.addAttribute("tab", tab);
        model.addAttribute("adjustment", new InventoryAdjustmentDto());

        return "admin/inventory";
    }

    @PostMapping("/adjustments")
    public String makeAdjustment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("adjustment") InventoryAdjustmentDto adjustmentDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid adjustment input. Reason and quantity change are required.");
            return "redirect:/admin/inventory";
        }

        try {
            inventoryService.adjustStock(
                    adjustmentDto.getProductId(),
                    adjustmentDto.getQuantityChange(),
                    adjustmentDto.getTransactionType(),
                    adjustmentDto.getReason(),
                    userDetails.getUser()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Stock adjusted successfully and audit logged.");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adjustment error: " + ex.getMessage());
        }

        return "redirect:/admin/inventory";
    }
}

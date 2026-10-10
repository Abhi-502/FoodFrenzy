package com.foodfrenzy.controller;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.entity.Cart;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.security.CustomUserDetails;
import com.foodfrenzy.service.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Cart cart = cartService.getCartForUser(user);
        CartSummaryDto summary = cartService.calculateSummary(cart);

        model.addAttribute("cart", cart);
        model.addAttribute("summary", summary);
        return "cart";
    }

    @PostMapping("/items")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam("productId") Long productId,
                            @RequestParam(name = "quantity", defaultValue = "1") int quantity,
                            @RequestHeader(value = "Referer", required = false) String referer,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(userDetails.getUser(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item successfully added to your cart!");
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not add item to cart: " + ex.getMessage());
        }

        if (referer != null && (referer.contains("/menu") || referer.contains("/products/"))) {
            return "redirect:" + referer;
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/quantity")
    public String updateQuantity(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @PathVariable("productId") Long productId,
                                 @RequestParam("quantity") int quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(userDetails.getUser(), productId, quantity);
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/items/{productId}/remove")
    public String removeItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @PathVariable("productId") Long productId,
                             RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(userDetails.getUser(), productId);
        redirectAttributes.addFlashAttribute("infoMessage", "Item removed from your cart.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        cartService.clearCart(userDetails.getUser());
        redirectAttributes.addFlashAttribute("infoMessage", "Your cart has been cleared.");
        return "redirect:/cart";
    }
}

package com.foodfrenzy.controller;

import com.foodfrenzy.entity.User;
import com.foodfrenzy.security.CustomUserDetails;
import com.foodfrenzy.service.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;

    public GlobalControllerAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return (userDetails != null) ? userDetails.getUser() : null;
    }

    @ModelAttribute("cartCount")
    public int getCartCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null && userDetails.getUser() != null) {
            return cartService.getCartItemCount(userDetails.getUser());
        }
        return 0;
    }
}

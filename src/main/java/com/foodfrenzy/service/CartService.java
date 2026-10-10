package com.foodfrenzy.service;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.entity.Cart;
import com.foodfrenzy.entity.User;

public interface CartService {
    Cart getCartForUser(User user);
    Cart addToCart(User user, Long productId, int quantity);
    Cart updateQuantity(User user, Long productId, int quantity);
    Cart removeFromCart(User user, Long productId);
    void clearCart(User user);
    CartSummaryDto calculateSummary(Cart cart);
    int getCartItemCount(User user);
}

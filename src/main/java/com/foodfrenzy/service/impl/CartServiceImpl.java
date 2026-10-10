package com.foodfrenzy.service.impl;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.entity.Cart;
import com.foodfrenzy.entity.CartItem;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.repository.CartItemRepository;
import com.foodfrenzy.repository.CartRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.service.CartService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Value("${app.order.tax-rate:0.08}")
    private double taxRate;

    @Value("${app.order.delivery-fee:4.99}")
    private double defaultDeliveryFee;

    @Value("${app.order.free-delivery-threshold:50.00}")
    private double freeDeliveryThreshold;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Cart getCartForUser(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart(user);
                    return cartRepository.save(cart);
                });
    }

    @Override
    public Cart addToCart(User user, Long productId, int quantity) {
        Cart cart = getCartForUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is currently unavailable.");
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int targetQuantity = quantity;
        if (existingItemOpt.isPresent()) {
            targetQuantity += existingItemOpt.get().getQuantity();
        }

        if (targetQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException("Cannot add " + quantity + " items. Available stock: " + product.getStockQuantity());
        }

        if (existingItemOpt.isPresent()) {
            CartItem item = existingItemOpt.get();
            item.setQuantity(targetQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.addItem(newItem);
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateQuantity(User user, Long productId, int quantity) {
        Cart cart = getCartForUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isEmpty()) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        CartItem item = existingItemOpt.get();
        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            if (quantity > product.getStockQuantity()) {
                throw new InsufficientStockException("Cannot set quantity to " + quantity + ". Maximum available: " + product.getStockQuantity());
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return cartRepository.save(cart);
    }

    @Override
    public Cart removeFromCart(User user, Long productId) {
        Cart cart = getCartForUser(user);
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        existingItemOpt.ifPresent(item -> {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        });

        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(User user) {
        Cart cart = getCartForUser(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartSummaryDto calculateSummary(Cart cart) {
        if (cart == null || cart.getItems().isEmpty()) {
            BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return new CartSummaryDto(zero, zero, zero, zero, 0, false);
        }

        BigDecimal subtotal = cart.getSubtotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(taxRate)).setScale(2, RoundingMode.HALF_UP);

        boolean freeDelivery = subtotal.compareTo(BigDecimal.valueOf(freeDeliveryThreshold)) >= 0;
        BigDecimal deliveryFee = freeDelivery ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(defaultDeliveryFee).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(tax).add(deliveryFee).setScale(2, RoundingMode.HALF_UP);
        int totalItems = cart.getTotalQuantity();

        return new CartSummaryDto(subtotal, tax, deliveryFee, total, totalItems, freeDelivery);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCartItemCount(User user) {
        if (user == null) return 0;
        return cartRepository.findByUserId(user.getId())
                .map(Cart::getTotalQuantity)
                .orElse(0);
    }
}

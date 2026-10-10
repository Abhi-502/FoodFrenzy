package com.foodfrenzy.service;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.entity.Cart;
import com.foodfrenzy.entity.CartItem;
import com.foodfrenzy.entity.Category;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.repository.CartItemRepository;
import com.foodfrenzy.repository.CartRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cartService, "taxRate", 0.08);
        ReflectionTestUtils.setField(cartService, "defaultDeliveryFee", 4.99);
        ReflectionTestUtils.setField(cartService, "freeDeliveryThreshold", 50.00);

        testUser = new User("Alex", "alex@example.com", "pass123", "555-0100", "123 Main St");
        testUser.setId(1L);

        testCart = new Cart(testUser);
        testCart.setId(5L);

        Category cat = new Category("Pizzas", "Wood-fired", "pizza.jpg", "bi-pie", 1);
        testProduct = new Product("Margherita", "Fresh basil", cat, new BigDecimal("15.00"), "margherita.jpg", 10);
        testProduct.setId(20L);
    }

    @Test
    @DisplayName("Should successfully add product to cart when stock is sufficient")
    void testAddToCartSuccess() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(testProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart updated = cartService.addToCart(testUser, 20L, 2);

        assertNotNull(updated);
        assertEquals(1, testCart.getItems().size());
        assertEquals(2, testCart.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(testCart);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when adding more items than available in stock")
    void testAddToCartInsufficientStock() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(testProduct));

        // Available stock is 10, requesting 15
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(testUser, 20L, 15));
        verify(cartRepository, never()).save(testCart);
    }

    @Test
    @DisplayName("Should correctly calculate cart totals with tax and standard delivery fee")
    void testCalculateSummaryStandardDelivery() {
        CartItem item = new CartItem(testCart, testProduct, 2); // 2 * $15.00 = $30.00
        testCart.addItem(item);

        CartSummaryDto summary = cartService.calculateSummary(testCart);

        assertNotNull(summary);
        assertEquals(new BigDecimal("30.00"), summary.getSubtotal());
        assertEquals(new BigDecimal("2.40"), summary.getTax()); // 8% of $30.00
        assertEquals(new BigDecimal("4.99"), summary.getDeliveryFee()); // Subtotal < $50.00
        assertEquals(new BigDecimal("37.39"), summary.getTotal()); // 30 + 2.40 + 4.99
        assertFalse(summary.isFreeDelivery());
    }

    @Test
    @DisplayName("Should apply free delivery when subtotal meets threshold")
    void testCalculateSummaryFreeDelivery() {
        CartItem item = new CartItem(testCart, testProduct, 4); // 4 * $15.00 = $60.00 (>= $50.00)
        testCart.addItem(item);

        CartSummaryDto summary = cartService.calculateSummary(testCart);

        assertNotNull(summary);
        assertEquals(new BigDecimal("60.00"), summary.getSubtotal());
        assertEquals(new BigDecimal("4.80"), summary.getTax());
        assertEquals(BigDecimal.ZERO.setScale(2), summary.getDeliveryFee());
        assertEquals(new BigDecimal("64.80"), summary.getTotal());
        assertTrue(summary.isFreeDelivery());
    }
}

package com.foodfrenzy.service;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.dto.CheckoutDto;
import com.foodfrenzy.entity.*;
import com.foodfrenzy.enums.OrderStatus;
import com.foodfrenzy.enums.PaymentMethod;
import com.foodfrenzy.enums.PaymentStatus;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.exception.InvalidOrderStateException;
import com.foodfrenzy.repository.OrderRepository;
import com.foodfrenzy.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CheckoutDto checkoutDto;

    @BeforeEach
    void setUp() {
        testUser = new User("Sarah", "sarah@example.com", "pass", "555-0100", "742 Evergreen");
        testUser.setId(100L);

        testCart = new Cart(testUser);
        testCart.setId(200L);

        Category cat = new Category("Burgers", "Burgers", "b.jpg", "bi-egg", 1);
        testProduct = new Product("Smash Burger", "Double beef", cat, new BigDecimal("12.00"), "img.jpg", 15);
        testProduct.setId(300L);

        CartItem item = new CartItem(testCart, testProduct, 2);
        testCart.addItem(item);

        checkoutDto = new CheckoutDto();
        checkoutDto.setRecipientName("Sarah Jenkins");
        checkoutDto.setRecipientPhone("+1 555-0145");
        checkoutDto.setStreet("742 Evergreen Terrace");
        checkoutDto.setCity("Springfield");
        checkoutDto.setState("IL");
        checkoutDto.setPostalCode("62704");
        checkoutDto.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
    }

    @Test
    @DisplayName("Should successfully place order, deduct inventory, and clear cart")
    void testPlaceOrderSuccess() {
        CartSummaryDto summary = new CartSummaryDto(
                new BigDecimal("24.00"),
                new BigDecimal("1.92"),
                new BigDecimal("4.99"),
                new BigDecimal("30.91"),
                2,
                false
        );

        when(cartService.getCartForUser(testUser)).thenReturn(testCart);
        when(inventoryService.hasSufficientStock(300L, 2)).thenReturn(true);
        when(cartService.calculateSummary(testCart)).thenReturn(summary);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(500L);
            return o;
        });

        Order placed = orderService.placeOrder(testUser, checkoutDto);

        assertNotNull(placed);
        assertEquals(500L, placed.getId());
        assertEquals(OrderStatus.PLACED, placed.getOrderStatus());
        assertEquals(new BigDecimal("30.91"), placed.getTotalAmount());
        assertTrue(placed.getOrderNumber().startsWith("FF-"));

        verify(inventoryService, times(1)).deductStockForOrder(eq(300L), eq(2), eq(testUser), anyString());
        verify(cartService, times(1)).clearCart(testUser);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should abort order placement and throw InsufficientStockException if product is out of stock")
    void testPlaceOrderInsufficientStock() {
        when(cartService.getCartForUser(testUser)).thenReturn(testCart);
        when(inventoryService.hasSufficientStock(300L, 2)).thenReturn(false);

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder(testUser, checkoutDto));

        verify(inventoryService, never()).deductStockForOrder(anyLong(), anyInt(), any(), anyString());
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart(any());
    }

    @Test
    @DisplayName("Should cancel eligible order and restore stock to inventory")
    void testCancelOrderByCustomerSuccess() {
        Order order = new Order();
        order.setId(501L);
        order.setOrderNumber("FF-20261010-TEST01");
        order.setUser(testUser);
        order.setOrderStatus(OrderStatus.PLACED);

        OrderItem item = new OrderItem(order, testProduct, "Smash Burger", new BigDecimal("12.00"), 2, "img.jpg");
        order.addItem(item);

        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order cancelled = orderService.cancelOrderByCustomer(501L, 100L, "Changed my mind");

        assertEquals(OrderStatus.CANCELLED, cancelled.getOrderStatus());
        verify(inventoryService, times(1)).restoreStockForOrder(300L, 2, testUser, "FF-20261010-TEST01");
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("Should prevent cancelling order if it is already out for delivery or delivered")
    void testCancelOrderDisallowedState() {
        Order order = new Order();
        order.setId(502L);
        order.setOrderNumber("FF-20261010-TEST02");
        order.setUser(testUser);
        order.setOrderStatus(OrderStatus.OUT_FOR_DELIVERY);

        when(orderRepository.findById(502L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrderByCustomer(502L, 100L, "Too late"));

        verify(inventoryService, never()).restoreStockForOrder(anyLong(), anyInt(), any(), anyString());
    }
}

package com.foodfrenzy.service;

import com.foodfrenzy.entity.Category;
import com.foodfrenzy.entity.InventoryTransaction;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.enums.TransactionType;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.repository.InventoryTransactionRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product testProduct;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        Category cat = new Category("Burgers", "Burgers", "img.jpg", "bi-egg", 1);
        testProduct = new Product("Smash Burger", "Double beef", cat, new BigDecimal("12.99"), "burger.jpg", 20);
        testProduct.setId(15L);

        testAdmin = new User("Admin", "admin@foodfrenzy.com", "pass", "555-0100", "HQ");
        testAdmin.setId(1L);
    }

    @Test
    @DisplayName("Should increase stock and log audit transaction on restock")
    void testRestockSuccess() {
        when(productRepository.findById(15L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product updated = inventoryService.adjustStock(15L, 10, TransactionType.STOCK_IN, "Weekly supplier delivery", testAdmin);

        assertEquals(30, updated.getStockQuantity());
        verify(productRepository, times(1)).save(testProduct);

        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        InventoryTransaction tx = captor.getValue();
        assertEquals(TransactionType.STOCK_IN, tx.getTransactionType());
        assertEquals(10, tx.getQuantityChange());
        assertEquals(30, tx.getRemainingStock());
        assertEquals("Weekly supplier delivery", tx.getReason());
    }

    @Test
    @DisplayName("Should prevent reducing stock below zero and throw InsufficientStockException")
    void testAdjustStockBelowZeroPrevented() {
        when(productRepository.findById(15L)).thenReturn(Optional.of(testProduct));

        // Current stock is 20, attempting to deduct 25
        assertThrows(InsufficientStockException.class, () ->
                inventoryService.adjustStock(15L, -25, TransactionType.STOCK_OUT, "Kitchen waste", testAdmin)
        );

        verify(productRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deduct stock for order fulfillment and log audit trail")
    void testDeductStockForOrder() {
        when(productRepository.findById(15L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        inventoryService.deductStockForOrder(15L, 3, testAdmin, "FF-20261010-0001");

        assertEquals(17, testProduct.getStockQuantity());

        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        InventoryTransaction tx = captor.getValue();
        assertEquals(TransactionType.ORDER_HOLD, tx.getTransactionType());
        assertEquals(-3, tx.getQuantityChange());
        assertEquals(17, tx.getRemainingStock());
    }
}

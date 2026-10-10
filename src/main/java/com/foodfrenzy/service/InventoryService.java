package com.foodfrenzy.service;

import com.foodfrenzy.entity.InventoryTransaction;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {
    Product adjustStock(Long productId, int quantityChange, TransactionType type, String reason, User performedBy);
    void deductStockForOrder(Long productId, int quantity, User user, String orderNumber);
    void restoreStockForOrder(Long productId, int quantity, User user, String orderNumber);
    boolean hasSufficientStock(Long productId, int quantity);
    List<InventoryTransaction> getTransactionsForProduct(Long productId);
    Page<InventoryTransaction> getAllTransactions(Pageable pageable);
}

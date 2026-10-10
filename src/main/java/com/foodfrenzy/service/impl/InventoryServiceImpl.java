package com.foodfrenzy.service.impl;

import com.foodfrenzy.entity.InventoryTransaction;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.enums.TransactionType;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.repository.InventoryTransactionRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryServiceImpl(ProductRepository productRepository,
                                InventoryTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Product adjustStock(Long productId, int quantityChange, TransactionType type, String reason, User performedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int newStock = product.getStockQuantity() + quantityChange;
        if (newStock < 0) {
            throw new InsufficientStockException("Cannot reduce stock below 0. Current stock is " + product.getStockQuantity());
        }

        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);

        InventoryTransaction transaction = new InventoryTransaction(
                updatedProduct,
                type,
                quantityChange,
                newStock,
                reason,
                performedBy
        );
        transactionRepository.save(transaction);

        return updatedProduct;
    }

    @Override
    public void deductStockForOrder(Long productId, int quantity, User user, String orderNumber) {
        adjustStock(
                productId,
                -quantity,
                TransactionType.ORDER_HOLD,
                "Order fulfillment for #" + orderNumber,
                user
        );
    }

    @Override
    public void restoreStockForOrder(Long productId, int quantity, User user, String orderNumber) {
        adjustStock(
                productId,
                quantity,
                TransactionType.ORDER_RESTORE,
                "Restocked due to cancellation of Order #" + orderNumber,
                user
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return product.getStockQuantity() >= quantity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryTransaction> getTransactionsForProduct(Long productId) {
        return transactionRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}

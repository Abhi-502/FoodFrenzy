package com.foodfrenzy.dto;

import com.foodfrenzy.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InventoryAdjustmentDto {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType = TransactionType.MANUAL_ADJUSTMENT;

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    @NotBlank(message = "Reason is required for inventory adjustments")
    private String reason;

    public InventoryAdjustmentDto() {}

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

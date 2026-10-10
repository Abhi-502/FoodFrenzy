package com.foodfrenzy.enums;

public enum TransactionType {
    STOCK_IN("Stock Added"),
    STOCK_OUT("Stock Deducted"),
    ORDER_HOLD("Deducted for Order"),
    ORDER_RESTORE("Restored from Order Cancellation"),
    MANUAL_ADJUSTMENT("Manual Stock Adjustment");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

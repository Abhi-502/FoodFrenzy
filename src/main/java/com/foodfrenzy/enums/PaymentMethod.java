package com.foodfrenzy.enums;

public enum PaymentMethod {
    CASH_ON_DELIVERY("Cash on Delivery"),
    CREDIT_CARD("Credit / Debit Card"),
    UPI("UPI / Instant Transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

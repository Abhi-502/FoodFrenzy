package com.foodfrenzy.enums;

public enum OrderStatus {
    PLACED("Placed", "Your order has been received and is waiting for confirmation"),
    CONFIRMED("Confirmed", "Your order has been confirmed by the restaurant"),
    PREPARING("Preparing", "Our chef is preparing your delicious meal"),
    OUT_FOR_DELIVERY("Out for Delivery", "Your food is on the way to your doorstep"),
    DELIVERED("Delivered", "Delivered successfully! Enjoy your meal"),
    CANCELLED("Cancelled", "Order was cancelled");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(OrderStatus next) {
        if (this == CANCELLED || this == DELIVERED) {
            return false;
        }
        if (next == CANCELLED) {
            // Can be cancelled only before it is out for delivery or delivered
            return this == PLACED || this == CONFIRMED;
        }
        return switch (this) {
            case PLACED -> next == CONFIRMED;
            case CONFIRMED -> next == PREPARING;
            case PREPARING -> next == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == DELIVERED;
            default -> false;
        };
    }
}

package com.foodfrenzy.dto;

import com.foodfrenzy.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateDto {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    private String reason;

    public OrderStatusUpdateDto() {}

    public OrderStatusUpdateDto(OrderStatus status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

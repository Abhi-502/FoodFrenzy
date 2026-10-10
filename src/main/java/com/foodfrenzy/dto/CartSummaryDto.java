package com.foodfrenzy.dto;

import java.math.BigDecimal;

public class CartSummaryDto {

    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private int itemCount = 0;
    private boolean freeDelivery = false;

    public CartSummaryDto() {}

    public CartSummaryDto(BigDecimal subtotal, BigDecimal tax, BigDecimal deliveryFee, BigDecimal total, int itemCount, boolean freeDelivery) {
        this.subtotal = subtotal;
        this.tax = tax;
        this.deliveryFee = deliveryFee;
        this.total = total;
        this.itemCount = itemCount;
        this.freeDelivery = freeDelivery;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public boolean isFreeDelivery() {
        return freeDelivery;
    }

    public void setFreeDelivery(boolean freeDelivery) {
        this.freeDelivery = freeDelivery;
    }
}

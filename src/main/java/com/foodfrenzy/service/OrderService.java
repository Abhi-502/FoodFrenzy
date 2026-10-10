package com.foodfrenzy.service;

import com.foodfrenzy.dto.CheckoutDto;
import com.foodfrenzy.entity.Order;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    Order placeOrder(User user, CheckoutDto checkoutDto);
    Order getOrderById(Long id);
    Order getOrderForCustomer(Long orderId, Long customerId);
    Page<Order> getCustomerOrders(Long customerId, Pageable pageable);
    Order cancelOrderByCustomer(Long orderId, Long customerId, String reason);
    Order updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus, String reason, User admin);
    Page<Order> getAllOrdersAdmin(OrderStatus status, String search, Pageable pageable);
    long countOrdersByStatus(OrderStatus status);
    long countTotalOrders();
}

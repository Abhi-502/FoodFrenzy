package com.foodfrenzy.service.impl;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.dto.CheckoutDto;
import com.foodfrenzy.entity.*;
import com.foodfrenzy.enums.OrderStatus;
import com.foodfrenzy.enums.PaymentStatus;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.exception.InvalidOrderStateException;
import com.foodfrenzy.exception.ResourceNotFoundException;
import com.foodfrenzy.exception.UnauthorizedAccessException;
import com.foodfrenzy.repository.OrderRepository;
import com.foodfrenzy.service.CartService;
import com.foodfrenzy.service.InventoryService;
import com.foodfrenzy.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartService cartService,
                            InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Order placeOrder(User user, CheckoutDto dto) {
        Cart cart = cartService.getCartForUser(user);
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Your cart is empty. Please add items before placing an order.");
        }

        // 1. Verify stock availability for all items before any changes
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!inventoryService.hasSufficientStock(product.getId(), cartItem.getQuantity())) {
                throw new InsufficientStockException("Insufficient stock for item '" + product.getName() + "'. Available: " + product.getStockQuantity());
            }
        }

        // 2. Generate unique order number
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String orderNumber = "FF-" + datePart + "-" + randomSuffix;

        // 3. Calculate financial totals on the server
        CartSummaryDto summary = cartService.calculateSummary(cart);

        // 4. Create Order entity
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PLACED);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setRecipientName(dto.getRecipientName());
        order.setRecipientPhone(dto.getRecipientPhone());
        order.setDeliveryAddress(dto.getFormattedDeliveryAddress());
        order.setSpecialInstructions(dto.getSpecialInstructions());
        order.setSubtotal(summary.getSubtotal());
        order.setTaxAmount(summary.getTax());
        order.setDeliveryFee(summary.getDeliveryFee());
        order.setTotalAmount(summary.getTotal());

        // 5. Create OrderItems with price snapshot & deduct inventory
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    product.getPrice(),
                    cartItem.getQuantity(),
                    product.getImageUrl()
            );
            order.addItem(orderItem);

            // Deduct stock and log audit transaction
            inventoryService.deductStockForOrder(product.getId(), cartItem.getQuantity(), user, orderNumber);
        }

        // 6. Save order persistently
        Order savedOrder = orderRepository.save(order);

        // 7. Clear the cart upon successful order creation
        cartService.clearCart(user);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderForCustomer(Long orderId, Long customerId) {
        Order order = getOrderById(orderId);
        if (!order.getUser().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("You are not authorized to view this order.");
        }
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getCustomerOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public Order cancelOrderByCustomer(Long orderId, Long customerId, String reason) {
        Order order = getOrderForCustomer(orderId, customerId);

        if (!order.canBeCancelled()) {
            throw new InvalidOrderStateException("Order #" + order.getOrderNumber() +
                    " cannot be cancelled in status: " + order.getOrderStatus().getDisplayName());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason("Cancelled by customer: " + (reason != null ? reason : "No reason provided"));

        // Restore inventory for all items
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                inventoryService.restoreStockForOrder(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        order.getUser(),
                        order.getOrderNumber()
                );
            }
        }

        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderStatusByAdmin(Long orderId, OrderStatus newStatus, String reason, User admin) {
        Order order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == newStatus) {
            return order;
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidOrderStateException("Cannot transition order #" + order.getOrderNumber() +
                    " from " + currentStatus.getDisplayName() + " to " + newStatus.getDisplayName());
        }

        order.setOrderStatus(newStatus);
        if (newStatus == OrderStatus.CANCELLED) {
            order.setCancellationReason("Cancelled by admin (" + admin.getName() + "): " + (reason != null ? reason : "Administrative cancellation"));
            // Restore inventory
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    inventoryService.restoreStockForOrder(
                            item.getProduct().getId(),
                            item.getQuantity(),
                            admin,
                            order.getOrderNumber()
                    );
                }
            }
        } else if (newStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersAdmin(OrderStatus status, String search, Pageable pageable) {
        return orderRepository.findOrdersFiltered(status, search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByOrderStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalOrders() {
        return orderRepository.count();
    }
}

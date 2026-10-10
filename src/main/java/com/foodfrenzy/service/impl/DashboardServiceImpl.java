package com.foodfrenzy.service.impl;

import com.foodfrenzy.dto.DashboardStatsDto;
import com.foodfrenzy.entity.Order;
import com.foodfrenzy.entity.Product;
import com.foodfrenzy.enums.OrderStatus;
import com.foodfrenzy.repository.OrderRepository;
import com.foodfrenzy.repository.ProductRepository;
import com.foodfrenzy.repository.UserRepository;
import com.foodfrenzy.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DashboardServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalCustomers(userRepository.countCustomers());
        stats.setTotalProducts(productRepository.count());
        stats.setTotalOrders(orderRepository.count());

        long placed = orderRepository.countByOrderStatus(OrderStatus.PLACED);
        long confirmed = orderRepository.countByOrderStatus(OrderStatus.CONFIRMED);
        long preparing = orderRepository.countByOrderStatus(OrderStatus.PREPARING);
        long outForDelivery = orderRepository.countByOrderStatus(OrderStatus.OUT_FOR_DELIVERY);
        stats.setPendingOrders(placed + confirmed + preparing + outForDelivery);

        long completed = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        stats.setCompletedOrders(completed);

        BigDecimal totalSales = orderRepository.calculateTotalDeliveredSales();
        stats.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);

        int lowStockThreshold = 5;
        List<Product> lowStockList = productRepository.findLowStockProducts(lowStockThreshold);
        stats.setLowStockProducts(lowStockList);
        stats.setLowStockCount(lowStockList.size());

        List<Order> recentOrders = orderRepository.findRecentOrders(PageRequest.of(0, 10));
        stats.setRecentOrders(recentOrders);

        return stats;
    }
}

package com.foodfrenzy.repository;

import com.foodfrenzy.entity.Order;
import com.foodfrenzy.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    long countByOrderStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.00) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateTotalDeliveredSales();

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.orderStatus = :status) AND (" +
           ":query IS NULL OR :query = '' OR " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.recipientName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.recipientPhone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Order> findOrdersFiltered(@Param("status") OrderStatus status,
                                   @Param("query") String query,
                                   Pageable pageable);

    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
    long countOrdersSince(@Param("since") LocalDateTime since);
}

package com.foodfrenzy.controller.admin;

import com.foodfrenzy.entity.Order;
import com.foodfrenzy.enums.OrderStatus;
import com.foodfrenzy.exception.InvalidOrderStateException;
import com.foodfrenzy.security.CustomUserDetails;
import com.foodfrenzy.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String listOrders(@RequestParam(name = "status", required = false) OrderStatus status,
                             @RequestParam(name = "search", required = false) String search,
                             @RequestParam(name = "page", defaultValue = "0") int page,
                             Model model) {
        Page<Order> orderPage = orderService.getAllOrdersAdmin(status, search, PageRequest.of(page, 15));

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("statuses", OrderStatus.values());

        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order-detail";
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable("id") Long id,
                                    @RequestParam("status") OrderStatus newStatus,
                                    @RequestParam(name = "reason", required = false) String reason,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatusByAdmin(id, newStatus, reason, userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage", "Order status successfully updated to " + newStatus.getDisplayName() + "!");
        } catch (InvalidOrderStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}

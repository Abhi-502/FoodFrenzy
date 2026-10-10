package com.foodfrenzy.controller;

import com.foodfrenzy.entity.Order;
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
@RequestMapping("/my-orders")
public class CustomerOrderController {

    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String myOrders(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(name = "page", defaultValue = "0") int page,
                           Model model) {
        Page<Order> orderPage = orderService.getCustomerOrders(userDetails.getUser().getId(), PageRequest.of(page, 10));
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        return "my-orders";
    }

    @GetMapping("/{id}")
    public String orderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable("id") Long id,
                              Model model) {
        Order order = orderService.getOrderForCustomer(id, userDetails.getUser().getId());
        model.addAttribute("order", order);
        return "order-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable("id") Long id,
                              @RequestParam(name = "reason", required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrderByCustomer(id, userDetails.getUser().getId(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Order has been cancelled and items were restored to stock.");
        } catch (InvalidOrderStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/my-orders/" + id;
    }
}

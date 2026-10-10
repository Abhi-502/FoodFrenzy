package com.foodfrenzy.controller;

import com.foodfrenzy.dto.CartSummaryDto;
import com.foodfrenzy.dto.CheckoutDto;
import com.foodfrenzy.entity.Cart;
import com.foodfrenzy.entity.Order;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.enums.PaymentMethod;
import com.foodfrenzy.exception.InsufficientStockException;
import com.foodfrenzy.security.CustomUserDetails;
import com.foodfrenzy.service.CartService;
import com.foodfrenzy.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Cart cart = cartService.getCartForUser(user);

        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        CartSummaryDto summary = cartService.calculateSummary(cart);

        if (!model.containsAttribute("checkout")) {
            CheckoutDto dto = new CheckoutDto();
            dto.setRecipientName(user.getName());
            dto.setRecipientPhone(user.getPhone() != null ? user.getPhone() : "");
            dto.setStreet(user.getAddress() != null ? user.getAddress() : "");
            dto.setCity("Springfield");
            dto.setState("IL");
            dto.setPostalCode("62704");
            dto.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
            model.addAttribute("checkout", dto);
        }

        model.addAttribute("cart", cart);
        model.addAttribute("summary", summary);
        return "checkout";
    }

    @PostMapping("/orders")
    public String placeOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @Valid @ModelAttribute("checkout") CheckoutDto checkoutDto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        User user = userDetails.getUser();
        Cart cart = cartService.getCartForUser(user);

        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty.");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            CartSummaryDto summary = cartService.calculateSummary(cart);
            model.addAttribute("cart", cart);
            model.addAttribute("summary", summary);
            return "checkout";
        }

        try {
            Order order = orderService.placeOrder(user, checkoutDto);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + order.getOrderNumber() + " placed successfully!");
            return "redirect:/orders/" + order.getId() + "/confirmation";
        } catch (InsufficientStockException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        } catch (Exception ex) {
            CartSummaryDto summary = cartService.calculateSummary(cart);
            model.addAttribute("cart", cart);
            model.addAttribute("summary", summary);
            model.addAttribute("errorMessage", "Could not complete order: " + ex.getMessage());
            return "checkout";
        }
    }

    @GetMapping("/orders/{id}/confirmation")
    public String orderConfirmation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable("id") Long id,
                                    Model model) {
        Order order = orderService.getOrderForCustomer(id, userDetails.getUser().getId());
        model.addAttribute("order", order);
        return "order-confirmation";
    }
}

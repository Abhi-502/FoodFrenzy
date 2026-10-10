package com.foodfrenzy.controller.admin;

import com.foodfrenzy.entity.User;
import com.foodfrenzy.exception.DuplicateEmailException;
import com.foodfrenzy.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final UserService userService;

    public AdminCustomerController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listCustomers(@RequestParam(name = "query", required = false) String query,
                                @RequestParam(name = "page", defaultValue = "0") int page,
                                Model model) {
        Page<User> userPage = userService.searchUsers(query, PageRequest.of(page, 10));
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("query", query);
        return "admin/customers";
    }

    @GetMapping("/new")
    public String newCustomerForm(Model model) {
        model.addAttribute("isNew", true);
        model.addAttribute("user", new User());
        return "admin/customer-form";
    }

    @PostMapping
    public String createCustomer(@RequestParam("name") String name,
                                 @RequestParam("email") String email,
                                 @RequestParam("password") String password,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("address") String address,
                                 @RequestParam(name = "role", defaultValue = "ROLE_CUSTOMER") String role,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.createUserByAdmin(name, email, password, phone, address, role);
            redirectAttributes.addFlashAttribute("successMessage", "Customer account created successfully!");
            return "redirect:/admin/customers";
        } catch (DuplicateEmailException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/customers/new";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCustomerForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getById(id);
        model.addAttribute("isNew", false);
        model.addAttribute("user", user);
        return "admin/customer-form";
    }

    @PostMapping("/{id}/edit")
    public String updateCustomer(@PathVariable("id") Long id,
                                 @RequestParam("name") String name,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("address") String address,
                                 @RequestParam(name = "active", defaultValue = "false") boolean active,
                                 @RequestParam(name = "role", defaultValue = "ROLE_CUSTOMER") String role,
                                 RedirectAttributes redirectAttributes) {
        userService.updateUserByAdmin(id, name, phone, address, active, role);
        redirectAttributes.addFlashAttribute("successMessage", "Customer updated successfully!");
        return "redirect:/admin/customers";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("infoMessage", "Customer status updated.");
        return "redirect:/admin/customers";
    }
}

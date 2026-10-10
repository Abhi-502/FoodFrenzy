package com.foodfrenzy.controller;

import com.foodfrenzy.dto.UserRegistrationDto;
import com.foodfrenzy.exception.DuplicateEmailException;
import com.foodfrenzy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {

        if (!registrationDto.isPasswordMatching()) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerCustomer(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please sign in with your email.");
            return "redirect:/login";
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "error.user", ex.getMessage());
            return "register";
        }
    }
}

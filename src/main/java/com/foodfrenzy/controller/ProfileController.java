package com.foodfrenzy.controller;

import com.foodfrenzy.dto.UserProfileDto;
import com.foodfrenzy.entity.User;
import com.foodfrenzy.security.CustomUserDetails;
import com.foodfrenzy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.getById(userDetails.getUser().getId());
        if (!model.containsAttribute("profile")) {
            UserProfileDto dto = new UserProfileDto(user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
            model.addAttribute("profile", dto);
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profile") UserProfileDto profileDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(userDetails.getUser().getId());
            model.addAttribute("user", user);
            return "profile";
        }

        User updatedUser = userService.updateProfile(userDetails.getUser().getId(), profileDto);
        // Refresh authenticated user cache if applicable
        userDetails.getUser().setName(updatedUser.getName());
        userDetails.getUser().setPhone(updatedUser.getPhone());
        userDetails.getUser().setAddress(updatedUser.getAddress());

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/profile";
    }
}

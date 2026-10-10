package com.foodfrenzy.controller;

import com.foodfrenzy.entity.User;
import com.foodfrenzy.service.CartService;
import com.foodfrenzy.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("GET /login should render login page")
    void testGetLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("GET /register should render register page")
    void testGetRegisterPage() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("POST /register should reject when passwords do not match")
    void testRegisterPasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("phone", "+1 555-0199")
                        .param("password", "secret123")
                        .param("confirmPassword", "different456")
                        .param("address", "123 Main St"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "confirmPassword"));
    }

    @Test
    @DisplayName("POST /register with valid details should redirect to /login")
    void testRegisterSuccess() throws Exception {
        User registered = new User("John Doe", "john@example.com", "hash", "+1 555-0199", "123 Main St");
        when(userService.registerCustomer(any())).thenReturn(registered);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("phone", "+1 555-0199")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123")
                        .param("address", "123 Main St"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

package com.foodfrenzy.controller;

import com.foodfrenzy.dto.DashboardStatsDto;
import com.foodfrenzy.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAccessSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("Anonymous users accessing /admin/dashboard must be redirected to /login")
    void testAnonymousAccessAdminRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "customer@foodfrenzy.com", roles = {"CUSTOMER"})
    @DisplayName("Customers with ROLE_CUSTOMER accessing /admin/dashboard must receive 403 Forbidden")
    void testCustomerForbiddenFromAdminArea() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@foodfrenzy.com", roles = {"ADMIN"})
    @DisplayName("Administrators with ROLE_ADMIN accessing /admin/dashboard should be allowed (200 OK)")
    void testAdminAuthorizedForAdminArea() throws Exception {
        when(dashboardService.getDashboardStats()).thenReturn(new DashboardStatsDto());

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }
}

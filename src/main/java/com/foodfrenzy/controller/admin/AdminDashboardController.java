package com.foodfrenzy.controller.admin;

import com.foodfrenzy.dto.DashboardStatsDto;
import com.foodfrenzy.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }
}

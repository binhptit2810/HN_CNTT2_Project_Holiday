package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.Ingredient;
import com.attraction.quanlinhahang.service.InventoryService;
import com.attraction.quanlinhahang.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        // Current Month Metrics
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().atTime(LocalTime.MAX);
        
        double revenue = reportService.getRevenue(startOfMonth, endOfMonth);
        long bills = reportService.getBillCount(startOfMonth, endOfMonth);
        Map<String, Integer> topItems = reportService.getTopSellingItems(startOfMonth, endOfMonth);

        model.addAttribute("monthlyRevenue", revenue);
        model.addAttribute("monthlyBillCount", bills);
        model.addAttribute("topItems", topItems);

        // Prepare data for Chart.js (Last 7 Days)
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            chartLabels.add(day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
            double dailyRev = reportService.getRevenue(day.atStartOfDay(), day.atTime(LocalTime.MAX));
            chartData.add(dailyRev);
        }
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);
        
        // Prepare Top Items for Chart.js (Top 5)
        List<String> topItemsLabels = new ArrayList<>();
        List<Integer> topItemsData = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> entry : topItems.entrySet()) {
            if (count >= 5) break;
            topItemsLabels.add(entry.getKey());
            topItemsData.add(entry.getValue());
            count++;
        }
        model.addAttribute("topItemsLabels", topItemsLabels);
        model.addAttribute("topItemsData", topItemsData);

        // Low stock warnings
        List<Ingredient> lowStock = new ArrayList<>();
        for (Ingredient ing : inventoryService.getAllIngredients()) {
            if (ing.getQuantityInStock() <= ing.getReorderLevel()) {
                lowStock.add(ing);
            }
        }
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("user", SecurityUtils.requireCurrentUser());

        return "admin/dashboard";
    }
}

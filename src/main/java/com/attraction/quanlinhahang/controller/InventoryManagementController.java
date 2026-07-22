package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventory")
public class InventoryManagementController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String inventoryPage(Model model) {
        model.addAttribute("ingredients", inventoryService.getAllIngredients());
        model.addAttribute("user", SecurityUtils.requireCurrentUser());
        return "admin/inventory";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addIngredient(@RequestParam String name, @RequestParam Double quantity, 
                                @RequestParam String unit, @RequestParam Double reorderLevel, 
                                RedirectAttributes attrs) {
        try {
            inventoryService.addIngredient(name, quantity, unit, reorderLevel);
            attrs.addFlashAttribute("success", "Thêm nguyên vật liệu thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventory";
    }

    @PostMapping("/update-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStock(@RequestParam Long id, @RequestParam Double addedStock, RedirectAttributes attrs) {
        try {
            inventoryService.updateStock(id, addedStock);
            attrs.addFlashAttribute("success", "Nhập kho nguyên vật liệu thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inventory";
    }
}

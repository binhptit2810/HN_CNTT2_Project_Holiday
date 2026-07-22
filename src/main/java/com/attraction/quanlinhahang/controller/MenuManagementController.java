package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.Ingredient;
import com.attraction.quanlinhahang.model.MenuItem;
import com.attraction.quanlinhahang.repository.IngredientRepository;
import com.attraction.quanlinhahang.repository.MenuItemRepository;
import com.attraction.quanlinhahang.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.attraction.quanlinhahang.service.CloudinaryService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/menu")
public class MenuManagementController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String menuPage(Model model) {
        List<MenuItem> items = menuItemRepository.findAll();
        Map<Long, List<com.attraction.quanlinhahang.model.MenuItemRecipe>> recipesMap = new java.util.HashMap<>();
        for (MenuItem item : items) {
            recipesMap.put(item.getId(), inventoryService.getRecipesForMenuItem(item.getId()));
        }
        
        model.addAttribute("menuItems", items);
        model.addAttribute("recipesMap", recipesMap);
        model.addAttribute("ingredients", ingredientRepository.findAll());
        model.addAttribute("user", SecurityUtils.requireCurrentUser());
        return "admin/menu";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addMenuItem(@RequestParam String name, @RequestParam Double price, 
                              @RequestParam String category, @RequestParam String description, 
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam(required = false) MultipartFile imageFile,
                              RedirectAttributes attrs) {
        try {
            String imgUrl = (imageUrl == null || imageUrl.trim().isEmpty()) ? "https://placehold.co/600x400?text=Food" : imageUrl.trim();
            if (imageFile != null && !imageFile.isEmpty()) {
                imgUrl = cloudinaryService.uploadImage(imageFile);
            }
            MenuItem item = MenuItem.builder()
                    .name(name)
                    .price(price)
                    .category(MenuItem.Category.valueOf(category.toUpperCase()))
                    .status(MenuItem.Status.AVAILABLE)
                    .description(description)
                    .imageUrl(imgUrl)
                    .build();
            menuItemRepository.save(item);
            attrs.addFlashAttribute("success", "Thêm món ăn thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editMenuItem(@RequestParam Long id, @RequestParam String name, @RequestParam Double price, 
                               @RequestParam String category, @RequestParam String description, 
                               @RequestParam(required = false) String imageUrl,
                               @RequestParam(required = false) MultipartFile imageFile,
                               RedirectAttributes attrs) {
        try {
            MenuItem item = menuItemRepository.findById(id).orElseThrow();
            item.setName(name);
            item.setPrice(price);
            item.setCategory(MenuItem.Category.valueOf(category.toUpperCase()));
            item.setDescription(description);
            String imgUrl = (imageUrl == null || imageUrl.trim().isEmpty()) ? item.getImageUrl() : imageUrl.trim();
            if (imageFile != null && !imageFile.isEmpty()) {
                imgUrl = cloudinaryService.uploadImage(imageFile);
            }
            item.setImageUrl(imgUrl);
            menuItemRepository.save(item);
            attrs.addFlashAttribute("success", "Cập nhật món ăn thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleMenuStatus(@RequestParam Long id, RedirectAttributes attrs) {
        try {
            MenuItem item = menuItemRepository.findById(id).orElseThrow();
            item.setStatus(item.getStatus() == MenuItem.Status.AVAILABLE ? MenuItem.Status.OUT_OF_STOCK : MenuItem.Status.AVAILABLE);
            menuItemRepository.save(item);
            attrs.addFlashAttribute("success", "Cập nhật trạng thái món ăn thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/recipe")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveRecipe(@RequestParam Long menuItemId, @RequestParam Long ingredientId, @RequestParam Double quantityRequired, RedirectAttributes attrs) {
        try {
            MenuItem item = menuItemRepository.findById(menuItemId).orElseThrow();
            Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow();
            inventoryService.saveRecipe(item, ingredient, quantityRequired);
            attrs.addFlashAttribute("success", "Cập nhật định lượng công thức thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/recipe/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteRecipe(@RequestParam Long recipeId, RedirectAttributes attrs) {
        try {
            inventoryService.deleteRecipe(recipeId);
            attrs.addFlashAttribute("success", "Đã xóa nguyên liệu khỏi công thức!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/menu";
    }
}

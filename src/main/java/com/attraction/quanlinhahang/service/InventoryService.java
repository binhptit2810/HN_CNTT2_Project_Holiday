package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuItemRecipeRepository recipeRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public Ingredient addIngredient(String name, double stock, String unit, double reorderLevel) {
        if (ingredientRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Ingredient already exists: " + name);
        }
        Ingredient ingredient = Ingredient.builder()
                .name(name)
                .quantityInStock(stock)
                .unit(unit)
                .reorderLevel(reorderLevel)
                .build();
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public Ingredient updateStock(Long id, double addedStock) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));
        ingredient.setQuantityInStock(ingredient.getQuantityInStock() + addedStock);
        Ingredient saved = ingredientRepository.save(ingredient);
        checkAndUpdateMenuAvailability();
        return saved;
    }

    public Ingredient editIngredient(Long id, String name, double reorderLevel, String unit) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingredient not found: " + id));
        ingredient.setName(name);
        ingredient.setReorderLevel(reorderLevel);
        ingredient.setUnit(unit);
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public void saveRecipe(MenuItem menuItem, Ingredient ingredient, double quantityRequired) {
        // Check if recipe already exists
        List<MenuItemRecipe> existing = recipeRepository.findByMenuItemId(menuItem.getId());
        MenuItemRecipe matched = existing.stream()
                .filter(r -> r.getIngredient().getId().equals(ingredient.getId()))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            matched.setQuantityRequired(quantityRequired);
            recipeRepository.save(matched);
        } else {
            MenuItemRecipe recipe = MenuItemRecipe.builder()
                    .menuItem(menuItem)
                    .ingredient(ingredient)
                    .quantityRequired(quantityRequired)
                    .build();
            recipeRepository.save(recipe);
        }
        checkAndUpdateMenuAvailability();
    }

    public List<MenuItemRecipe> getRecipesForMenuItem(Long menuItemId) {
        return recipeRepository.findByMenuItemId(menuItemId);
    }

    @Transactional
    public void deleteRecipe(Long recipeId) {
        recipeRepository.deleteById(recipeId);
        checkAndUpdateMenuAvailability();
    }

    @Transactional
    public void deductStockForOrderDetail(OrderDetail detail) {
        MenuItem item = detail.getMenuItem();
        List<MenuItemRecipe> recipes = recipeRepository.findByMenuItemId(item.getId());
        
        // Phase 1: Check availability
        for (MenuItemRecipe recipe : recipes) {
            Ingredient ingredient = recipe.getIngredient();
            double required = recipe.getQuantityRequired() * detail.getQuantity();
            if (ingredient.getQuantityInStock() < required) {
                throw new IllegalStateException("Không đủ nguyên liệu để nấu món: " + item.getName() + " (Thiếu " + ingredient.getName() + ")");
            }
        }
        
        // Phase 2: Deduct
        for (MenuItemRecipe recipe : recipes) {
            Ingredient ingredient = recipe.getIngredient();
            double required = recipe.getQuantityRequired() * detail.getQuantity();
            ingredient.setQuantityInStock(ingredient.getQuantityInStock() - required);
            ingredientRepository.save(ingredient);
        }
        
        checkAndUpdateMenuAvailability();
    }

    @Transactional
    public void checkAndUpdateMenuAvailability() {
        List<MenuItem> allItems = menuItemRepository.findAll();
        for (MenuItem item : allItems) {
            List<MenuItemRecipe> recipes = recipeRepository.findByMenuItemId(item.getId());
            boolean canCook = true;
            for (MenuItemRecipe recipe : recipes) {
                if (recipe.getIngredient().getQuantityInStock() < recipe.getQuantityRequired()) {
                    canCook = false;
                    break;
                }
            }
            
            if (canCook && item.getStatus() == MenuItem.Status.OUT_OF_STOCK) {
                item.setStatus(MenuItem.Status.AVAILABLE);
                menuItemRepository.save(item);
            } else if (!canCook && item.getStatus() == MenuItem.Status.AVAILABLE) {
                item.setStatus(MenuItem.Status.OUT_OF_STOCK);
                menuItemRepository.save(item);
            }
        }
    }
}


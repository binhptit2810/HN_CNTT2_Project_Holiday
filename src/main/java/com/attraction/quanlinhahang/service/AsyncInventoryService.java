package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service riêng biệt chuyên xử lý việc trừ tồn kho nguyên liệu bất đồng bộ.
 * Tách khỏi InventoryService để giải quyết xung đột @Async + @Transactional
 * khi gọi nội bộ cùng class (Spring proxy không bọc được transaction đúng).
 */
@Service
public class AsyncInventoryService {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private MenuItemRecipeRepository recipeRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Async
    @Transactional
    public void deductStockForOrder(Order order) {
        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        for (OrderDetail detail : details) {
            MenuItem item = detail.getMenuItem();
            List<MenuItemRecipe> recipes = recipeRepository.findByMenuItemId(item.getId());
            for (MenuItemRecipe recipe : recipes) {
                Ingredient ingredient = recipe.getIngredient();
                double required = recipe.getQuantityRequired() * detail.getQuantity();
                double currentStock = ingredient.getQuantityInStock();
                double newStock = Math.max(0.0, currentStock - required);
                ingredient.setQuantityInStock(newStock);
                ingredientRepository.save(ingredient);
            }
        }
    }
}

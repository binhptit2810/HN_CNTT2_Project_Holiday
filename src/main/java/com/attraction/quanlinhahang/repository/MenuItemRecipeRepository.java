package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.MenuItemRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRecipeRepository extends JpaRepository<MenuItemRecipe, Long> {
    List<MenuItemRecipe> findByMenuItemId(Long menuItemId);
}

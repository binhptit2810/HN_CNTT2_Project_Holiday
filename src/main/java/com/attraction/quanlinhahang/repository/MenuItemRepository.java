package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategory(MenuItem.Category category);
    List<MenuItem> findByNameContainingIgnoreCase(String query);
    java.util.Optional<MenuItem> findByName(String name);
}

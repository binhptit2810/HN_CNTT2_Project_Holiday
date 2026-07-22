package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.Cart;
import com.attraction.quanlinhahang.model.CartItem;
import com.attraction.quanlinhahang.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);
    List<CartItem> findByCart(Cart cart);
    void deleteByCartAndMenuItem(Cart cart, MenuItem menuItem);
}

package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.Cart;
import com.attraction.quanlinhahang.model.CartItem;
import com.attraction.quanlinhahang.model.MenuItem;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.repository.CartItemRepository;
import com.attraction.quanlinhahang.repository.CartRepository;
import com.attraction.quanlinhahang.repository.MenuItemRepository;
import com.attraction.quanlinhahang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public void addItem(Long userId, Long menuItemId, int quantity, String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0!");
        }
        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại!"));

        if (menuItem.getStatus() == MenuItem.Status.OUT_OF_STOCK) {
            throw new IllegalArgumentException("Món ăn này hiện đã hết hàng!");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndMenuItem(cart, menuItem);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            if (note != null && !note.trim().isEmpty()) {
                item.setNote(note);
            }
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .note(note)
                    .build();
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void updateQty(Long userId, Long menuItemId, int delta) {
        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại!"));

        CartItem item = cartItemRepository.findByCartAndMenuItem(cart, menuItem)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không có trong giỏ hàng!"));

        int newQty = item.getQuantity() + delta;
        if (newQty <= 0) {
            cartItemRepository.delete(item);
        } else {
            if (delta > 0 && menuItem.getStatus() == MenuItem.Status.OUT_OF_STOCK) {
                throw new IllegalArgumentException("Món ăn này đã hết hàng, không thể thêm tiếp!");
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeItem(Long userId, Long menuItemId) {
        Cart cart = getOrCreateCart(userId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại!"));
        cartItemRepository.deleteByCartAndMenuItem(cart, menuItem);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCart(cart);
        cartItemRepository.deleteAll(items);
    }

    @Transactional
    public List<CartItem> getCartItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCart(cart);
    }

    @Transactional
    public Double getCartTotal(Long userId) {
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .mapToDouble(item -> item.getMenuItem().getPrice() * item.getQuantity())
                .sum();
    }
}

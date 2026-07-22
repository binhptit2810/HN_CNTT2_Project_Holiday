package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.Favorite;
import com.attraction.quanlinhahang.model.MenuItem;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.repository.FavoriteRepository;
import com.attraction.quanlinhahang.repository.MenuItemRepository;
import com.attraction.quanlinhahang.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Transactional
    public boolean toggleFavorite(Long userId, Long menuItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại!"));

        Optional<Favorite> existing = favoriteRepository.findByUserAndMenuItem(user, menuItem);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // Removed from favorites
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .menuItem(menuItem)
                    .build();
            favoriteRepository.save(favorite);
            return true; // Added to favorites
        }
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getFavorites(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));
        List<Favorite> favorites = favoriteRepository.findByUser(user);
        return favorites.stream()
                .map(Favorite::getMenuItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, Long menuItemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại!"));
        return favoriteRepository.existsByUserAndMenuItem(user, menuItem);
    }
}

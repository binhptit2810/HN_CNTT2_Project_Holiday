package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.Favorite;
import com.attraction.quanlinhahang.model.MenuItem;
import com.attraction.quanlinhahang.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndMenuItem(User user, MenuItem menuItem);
    List<Favorite> findByUser(User user);
    boolean existsByUserAndMenuItem(User user, MenuItem menuItem);
    
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.menuItem.id = :menuItemId")
    void deleteByUserIdAndMenuItemId(Long userId, Long menuItemId);
}

package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    Optional<RestaurantTable> findByTableNumber(String tableNumber);
}

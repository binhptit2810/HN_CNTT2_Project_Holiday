package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(Order.Status status);
    List<Order> findByTableNumberAndStatusIn(String tableNumber, List<Order.Status> statuses);
}

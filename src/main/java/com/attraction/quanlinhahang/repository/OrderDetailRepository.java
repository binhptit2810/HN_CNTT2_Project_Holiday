package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.Order;
import com.attraction.quanlinhahang.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrder(Order order);
    List<OrderDetail> findByOrderId(Long orderId);
    List<OrderDetail> findByStatus(OrderDetail.Status status);
    List<OrderDetail> findByStatusIn(Collection<OrderDetail.Status> statuses);
}


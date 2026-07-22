package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.CustomerOrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRequestRepository extends JpaRepository<CustomerOrderRequest, Long> {
    List<CustomerOrderRequest> findByStatus(CustomerOrderRequest.Status status);
}

package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.CustomerOrderRequest;
import com.attraction.quanlinhahang.model.CustomerOrderRequestDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerOrderRequestDetailRepository extends JpaRepository<CustomerOrderRequestDetail, Long> {
    List<CustomerOrderRequestDetail> findByRequest(CustomerOrderRequest request);
    List<CustomerOrderRequestDetail> findByRequestId(Long requestId);
}

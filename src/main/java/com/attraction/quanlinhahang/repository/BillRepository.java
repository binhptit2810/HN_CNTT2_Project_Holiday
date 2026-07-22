package com.attraction.quanlinhahang.repository;

import com.attraction.quanlinhahang.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByOrderId(Long orderId);
    List<Bill> findByStatus(Bill.Status status);
    List<Bill> findByCreatedAtBetweenAndStatus(LocalDateTime start, LocalDateTime end, Bill.Status status);
}

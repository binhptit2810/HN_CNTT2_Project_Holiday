package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public double getRevenue(LocalDateTime start, LocalDateTime end) {
        List<Bill> bills = billRepository.findByCreatedAtBetweenAndStatus(start, end, Bill.Status.PAID);
        return bills.stream().mapToDouble(Bill::getTotalAmount).sum();
    }

    public long getBillCount(LocalDateTime start, LocalDateTime end) {
        List<Bill> bills = billRepository.findByCreatedAtBetweenAndStatus(start, end, Bill.Status.PAID);
        return bills.size();
    }

    public Map<String, Integer> getTopSellingItems(LocalDateTime start, LocalDateTime end) {
        List<Bill> bills = billRepository.findByCreatedAtBetweenAndStatus(start, end, Bill.Status.PAID);
        Map<String, Integer> itemCounts = new HashMap<>();

        for (Bill bill : bills) {
            List<OrderDetail> details = orderDetailRepository.findByOrderId(bill.getOrder().getId());
            for (OrderDetail detail : details) {
                String itemName = detail.getMenuItem().getName();
                itemCounts.put(itemName, itemCounts.getOrDefault(itemName, 0) + detail.getQuantity());
            }
        }

        // Sort by value descending
        return itemCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}

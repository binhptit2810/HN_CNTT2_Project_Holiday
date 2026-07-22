package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BillingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private AsyncInventoryService asyncInventoryService;

    public double calculateSubtotal(List<OrderDetail> details) {
        return details.stream()
                .mapToDouble(d -> d.getUnitPrice() * d.getQuantity())
                .sum();
    }

    @Transactional
    public Bill checkout(Long orderId, double discountAmount, double vatRate, Bill.PaymentMethod paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        List<Bill> existingBills = billRepository.findByOrderId(orderId);
        for (Bill b : existingBills) {
            if (b.getStatus() == Bill.Status.PAID) {
                throw new IllegalStateException("Hóa đơn cho đơn hàng này đã thanh toán! Không được phép thực hiện giao dịch thanh toán lại.");
            }
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder(order);
        if (details.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout empty order!");
        }

        double subtotal = calculateSubtotal(details);
        double tax = (subtotal - discountAmount) * vatRate;
        double total = Math.max(0.0, subtotal - discountAmount + tax);

        Bill bill = Bill.builder()
                .order(order)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .vatRate(vatRate)
                .totalAmount(total)
                .paymentMethod(paymentMethod)
                .status(Bill.Status.PAID)
                .build();
        billRepository.save(bill);

        // Update order status
        order.setStatus(Order.Status.PAID);
        orderRepository.save(order);

        // Clear the table (release table back to EMPTY status)
        RestaurantTable table = tableRepository.findByTableNumber(order.getTableNumber())
                .orElse(null);
        if (table != null) {
            table.setStatus(RestaurantTable.Status.EMPTY);
            table.setPaymentRequested(false);
            table.setCurrentCustomer(null);
            tableRepository.save(table);
        }

        return bill;
    }

    @Transactional
    public Order splitInvoice(Long orderId, List<Long> detailIdsToSplit) {
        Order originalOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Original order not found: " + orderId));

        if (detailIdsToSplit.isEmpty()) {
            throw new IllegalArgumentException("No items selected to split!");
        }

        // Create a new order for the split items
        Order newOrder = Order.builder()
                .tableNumber(originalOrder.getTableNumber() + " (Tách)")
                .status(Order.Status.PENDING)
                .build();
        orderRepository.save(newOrder);

        for (Long detailId : detailIdsToSplit) {
            OrderDetail detail = orderDetailRepository.findById(detailId)
                    .orElseThrow(() -> new IllegalArgumentException("Order detail not found: " + detailId));
            
            if (!detail.getOrder().getId().equals(orderId)) {
                throw new IllegalArgumentException("Order detail does not belong to the original order!");
            }

            // Move detail to new order
            detail.setOrder(newOrder);
            orderDetailRepository.save(detail);
        }

        // Check if original order is now empty. If so, cancel it.
        List<OrderDetail> originalDetails = orderDetailRepository.findByOrder(originalOrder);
        if (originalDetails.isEmpty()) {
            originalOrder.setStatus(Order.Status.CANCELLED);
            orderRepository.save(originalOrder);
        }

        return newOrder;
    }
}

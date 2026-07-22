package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private BillRepository billRepository;

    public Optional<Shift> getActiveShift(User user) {
        return shiftRepository.findByUserAndStatus(user, Shift.Status.OPEN);
    }

    public Shift openShift(User user, double startBalance) {
        if (getActiveShift(user).isPresent()) {
            throw new IllegalStateException("Bạn đã có một ca làm việc đang mở!");
        }
        Shift shift = Shift.builder()
                .user(user)
                .openedAt(LocalDateTime.now())
                .startBalance(startBalance)
                .status(Shift.Status.OPEN)
                .build();
        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift closeShift(User user, double endBalanceDeclared, String notes) {
        Shift shift = getActiveShift(user)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ca làm việc nào đang mở!"));

        // Calculate cash sales sum since shift.openedAt
        List<Bill> paidBills = billRepository.findByStatus(Bill.Status.PAID);
        double cashRevenue = paidBills.stream()
                .filter(b -> b.getCreatedAt().isAfter(shift.getOpenedAt()))
                .filter(b -> b.getPaymentMethod() == Bill.PaymentMethod.CASH)
                .mapToDouble(Bill::getTotalAmount)
                .sum();

        double calculated = shift.getStartBalance() + cashRevenue;
        double diff = endBalanceDeclared - calculated;

        shift.setClosedAt(LocalDateTime.now());
        shift.setEndBalanceDeclared(endBalanceDeclared);
        shift.setEndBalanceCalculated(calculated);
        shift.setDifference(diff);
        shift.setStatus(Shift.Status.CLOSED);
        shift.setNotes(notes);

        return shiftRepository.save(shift);
    }

    public List<Bill> getPaidBills(Shift shift) {
        return billRepository.findByStatus(Bill.Status.PAID).stream()
                .filter(b -> b.getCreatedAt().isAfter(shift.getOpenedAt()))
                .sorted(java.util.Comparator.comparing(Bill::getCreatedAt).reversed())
                .toList();
    }

    public Map<String, Double> getShiftRevenueDetails(Shift shift) {
        List<Bill> paidBills = billRepository.findByStatus(Bill.Status.PAID);
        double cashRevenue = paidBills.stream()
                .filter(b -> b.getCreatedAt().isAfter(shift.getOpenedAt()))
                .filter(b -> b.getPaymentMethod() == Bill.PaymentMethod.CASH)
                .mapToDouble(Bill::getTotalAmount)
                .sum();

        double bankingRevenue = paidBills.stream()
                .filter(b -> b.getCreatedAt().isAfter(shift.getOpenedAt()))
                .filter(b -> b.getPaymentMethod() == Bill.PaymentMethod.BANKING)
                .mapToDouble(Bill::getTotalAmount)
                .sum();

        double cardRevenue = paidBills.stream()
                .filter(b -> b.getCreatedAt().isAfter(shift.getOpenedAt()))
                .filter(b -> b.getPaymentMethod() == Bill.PaymentMethod.CARD)
                .mapToDouble(Bill::getTotalAmount)
                .sum();

        double totalRevenue = cashRevenue + bankingRevenue + cardRevenue;

        Map<String, Double> details = new HashMap<>();
        details.put("cash", cashRevenue);
        details.put("banking", bankingRevenue);
        details.put("card", cardRevenue);
        details.put("total", totalRevenue);
        return details;
    }
}

package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.Voucher;
import com.attraction.quanlinhahang.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    public void toggleVoucherStatus(Long id) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow();
        voucher.setActive(!voucher.getActive());
        voucherRepository.save(voucher);
    }

    public double calculateDiscount(String code, double orderTotal) {
        if (code == null || code.trim().isEmpty()) {
            return 0.0;
        }
        Optional<Voucher> optVoucher = voucherRepository.findByCode(code.trim().toUpperCase());
        if (optVoucher.isEmpty()) {
            throw new IllegalArgumentException("Mã giảm giá không hợp lệ!");
        }
        Voucher voucher = optVoucher.get();

        if (!voucher.getActive()) {
            throw new IllegalArgumentException("Mã giảm giá đã bị khóa!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày sử dụng!");
        }
        if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn!");
        }
        if (voucher.getMinOrderValue() != null && orderTotal < voucher.getMinOrderValue()) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue() + " đ để dùng mã này!");
        }

        double discountAmount = 0.0;
        if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            discountAmount = voucher.getDiscountValue();
        } else if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            discountAmount = orderTotal * (voucher.getDiscountValue() / 100.0);
            if (voucher.getMaxDiscountAmount() != null && discountAmount > voucher.getMaxDiscountAmount()) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        }
        
        return Math.min(discountAmount, orderTotal);
    }
}

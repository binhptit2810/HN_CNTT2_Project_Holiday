package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.Voucher;
import com.attraction.quanlinhahang.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherManagementController {

    @Autowired
    private VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String voucherPage(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        model.addAttribute("user", SecurityUtils.requireCurrentUser());
        return "admin/vouchers";
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public String addVoucher(@RequestParam String code, @RequestParam String discountType, 
                             @RequestParam Double discountValue, @RequestParam(required = false) Double minOrderValue, 
                             @RequestParam(required = false) Double maxDiscountAmount, 
                             @RequestParam(required = false) String validFrom, 
                             @RequestParam(required = false) String validTo, 
                             RedirectAttributes attrs) {
        try {
            Voucher voucher = Voucher.builder()
                    .code(code.trim().toUpperCase())
                    .discountType(Voucher.DiscountType.valueOf(discountType))
                    .discountValue(discountValue)
                    .minOrderValue(minOrderValue)
                    .maxDiscountAmount(maxDiscountAmount)
                    .validFrom(validFrom != null && !validFrom.isBlank() ? LocalDateTime.parse(validFrom) : null)
                    .validTo(validTo != null && !validTo.isBlank() ? LocalDateTime.parse(validTo) : null)
                    .active(true)
                    .build();
            voucherService.saveVoucher(voucher);
            attrs.addFlashAttribute("success", "Tạo mã giảm giá thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleVoucher(@RequestParam Long id, RedirectAttributes attrs) {
        try {
            voucherService.toggleVoucherStatus(id);
            attrs.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteVoucher(@RequestParam Long id, RedirectAttributes attrs) {
        try {
            voucherService.deleteVoucher(id);
            attrs.addFlashAttribute("success", "Đã xóa mã giảm giá!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/vouchers";
    }

    // Endpoint for POS to validate voucher
    @GetMapping("/validate")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public Map<String, Object> validateVoucher(@RequestParam String code, @RequestParam double orderTotal) {
        Map<String, Object> response = new HashMap<>();
        try {
            double discount = voucherService.calculateDiscount(code, orderTotal);
            response.put("success", true);
            response.put("discountAmount", discount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}

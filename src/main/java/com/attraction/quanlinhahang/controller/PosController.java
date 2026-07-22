package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.UserRepository;
import com.attraction.quanlinhahang.service.BillingService;
import com.attraction.quanlinhahang.service.ShiftService;
import com.attraction.quanlinhahang.service.TableOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/pos")
public class PosController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private TableOrderService tableOrderService;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.attraction.quanlinhahang.service.VoucherService voucherService;

    @org.springframework.beans.factory.annotation.Value("${vietqr.bank-id:MB}")
    private String bankId;

    @org.springframework.beans.factory.annotation.Value("${vietqr.account-no:}")
    private String accountNo;

    @org.springframework.beans.factory.annotation.Value("${vietqr.account-name:}")
    private String accountName;

    @GetMapping
    @PreAuthorize("hasRole('CASHIER')")
    public String posPage(Model model) {
        User user = SecurityUtils.requireCurrentUser();

        // Check if Cashier has an active open shift
        Optional<Shift> activeShift = shiftService.getActiveShift(user);
        if (activeShift.isEmpty()) {
            return "redirect:/admin/pos/open-shift";
        }
        model.addAttribute("activeShift", activeShift.get());
        
        // Tính số tiền đã thu trong cả ca làm việc
        Map<String, Double> revenueDetails = shiftService.getShiftRevenueDetails(activeShift.get());
        model.addAttribute("shiftRevenue", revenueDetails);

        List<RestaurantTable> tables = tableOrderService.getAllTables();
        List<Order> activeOrders = tableOrderService.getActiveOrders();
        List<Bill> paidBills = shiftService.getPaidBills(activeShift.get());
        
        model.addAttribute("tables", tables);
        model.addAttribute("activeOrders", activeOrders);
        model.addAttribute("paidBills", paidBills);
        model.addAttribute("user", user);
        
        model.addAttribute("vietqrBankId", bankId);
        model.addAttribute("vietqrAccountNo", accountNo);
        model.addAttribute("vietqrAccountName", accountName);

        Map<String, RestaurantTable> tableMap = new HashMap<>();
        for (RestaurantTable t : tables) {
            tableMap.put(t.getTableNumber(), t);
        }
        model.addAttribute("tableMap", tableMap);

        return "admin/pos";
    }

    @GetMapping("/open-shift")
    @PreAuthorize("hasRole('CASHIER')")
    public String openShiftPage(Model model) {
        User user = SecurityUtils.requireCurrentUser();
        
        if (shiftService.getActiveShift(user).isPresent()) {
            return "redirect:/admin/pos";
        }
        
        model.addAttribute("user", user);
        return "admin/open-shift";
    }

    @PostMapping("/open-shift")
    @PreAuthorize("hasRole('CASHIER')")
    public String openShift(@RequestParam Double startBalance, RedirectAttributes attrs) {
        User user = SecurityUtils.requireCurrentUser();
        try {
            shiftService.openShift(user, startBalance);
            attrs.addFlashAttribute("success", "Mở ca làm việc thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/pos";
    }

    @PostMapping("/close-shift")
    @PreAuthorize("hasRole('CASHIER')")
    public String closeShift(@RequestParam Double endBalanceDeclared, @RequestParam(required = false) String notes, RedirectAttributes attrs) {
        User user = SecurityUtils.requireCurrentUser();
        try {
            Shift shift = shiftService.closeShift(user, endBalanceDeclared, notes);
            attrs.addFlashAttribute("success", "Đóng ca làm việc thành công! Tiền đầu ca: " + shift.getStartBalance() + " VNĐ, Tiền tính toán cuối ca: " + shift.getEndBalanceCalculated() + " VNĐ, Tiền khai báo: " + shift.getEndBalanceDeclared() + " VNĐ, Chênh lệch: " + shift.getDifference() + " VNĐ.");
            return "redirect:/logout";
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
            return "redirect:/admin/pos";
        }
    }

    @GetMapping("/order/{orderId}")
    @ResponseBody
    @PreAuthorize("hasRole('CASHIER')")
    public Map<String, Object> getOrderDetailsForPOS(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();

        List<OrderDetail> details = tableOrderService.getOrderDetails(orderId);
        double subtotal = billingService.calculateSubtotal(details);

        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (OrderDetail d : details) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", d.getId());
            item.put("name", d.getMenuItem().getName());
            item.put("quantity", d.getQuantity());
            item.put("price", d.getUnitPrice());
            item.put("total", d.getUnitPrice() * d.getQuantity());
            item.put("note", d.getNote());
            itemsList.add(item);
        }

        response.put("items", itemsList);
        response.put("subtotal", subtotal);
        return response;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('CASHIER')")
    public String doCheckout(@RequestParam Long orderId, 
                             @RequestParam(required = false) String voucherCode,
                             @RequestParam(defaultValue = "0") Double manualDiscount, 
                             @RequestParam(defaultValue = "0.1") Double vat, 
                             @RequestParam String paymentMethod, 
                             RedirectAttributes attrs) {
        try {
            double finalDiscount = manualDiscount;
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                List<OrderDetail> details = tableOrderService.getOrderDetails(orderId);
                double orderTotal = billingService.calculateSubtotal(details);
                // We need VoucherService for this! Let's inject it. Wait, I should add @Autowired VoucherService at the top.
                finalDiscount = voucherService.calculateDiscount(voucherCode, orderTotal);
            }

            Bill.PaymentMethod pm = Bill.PaymentMethod.valueOf(paymentMethod.toUpperCase());
            Bill bill = billingService.checkout(orderId, finalDiscount, vat, pm);
            attrs.addFlashAttribute("success", "Thanh toán thành công hóa đơn #" + bill.getId() + "! Tổng tiền: " + bill.getTotalAmount() + " VNĐ.");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/pos";
    }

    @PostMapping("/split")
    @PreAuthorize("hasRole('CASHIER')")
    public String doSplit(@RequestParam Long orderId, @RequestParam List<Long> detailIds, RedirectAttributes attrs) {
        try {
            Order newOrder = billingService.splitInvoice(orderId, detailIds);
            attrs.addFlashAttribute("success", "Đã tách hóa đơn thành công! Đơn hàng tách mang số bàn: " + newOrder.getTableNumber());
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/pos";
    }

    @PostMapping("/approve-edit")
    @PreAuthorize("hasRole('CASHIER')")
    public String approveEdit(
            @RequestParam Long detailId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(defaultValue = "false") Boolean delete,
            @RequestParam String managerPassword,
            RedirectAttributes attrs) {
        try {
            // Check manager password against admin role accounts (dùng BCrypt)
            List<User> managers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == User.Role.ADMIN)
                    .toList();
            
            boolean authorized = false;
            for (User m : managers) {
                if (passwordEncoder.matches(managerPassword, m.getPassword())) {
                    authorized = true;
                    break;
                }
            }

            if (!authorized) {
                attrs.addFlashAttribute("error", "Xác nhận thất bại: Mật khẩu Quản lý không chính xác!");
                return "redirect:/admin/pos";
            }

            tableOrderService.editOrDeleteOrderDetail(detailId, quantity, delete);
            attrs.addFlashAttribute("success", "Đã phê duyệt chỉnh sửa món ăn thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", com.attraction.quanlinhahang.util.ExceptionUtils.getFriendlyMessage(e));
        }
        return "redirect:/admin/pos";
    }
}

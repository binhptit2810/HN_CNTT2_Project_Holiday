package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.Bill;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/bills")
public class BillHistoryController {

    @Autowired
    private BillRepository billRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    public String listBills(Model model) {
        User user = SecurityUtils.requireCurrentUser();
        List<Bill> bills = billRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        model.addAttribute("bills", bills);
        model.addAttribute("user", user);
        return "admin/bills";
    }
}

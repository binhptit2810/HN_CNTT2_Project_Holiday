package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.OrderDetail;
import com.attraction.quanlinhahang.model.User;
import com.attraction.quanlinhahang.repository.OrderDetailRepository;
import com.attraction.quanlinhahang.service.TableOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/kitchen")
public class KitchenController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private TableOrderService tableOrderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public String kitchenPage(Model model) {
        User user = SecurityUtils.requireCurrentUser();

        // Fetch active order details (PENDING, COOKING, READY) sorted FIFO
        List<OrderDetail> activeDetails = orderDetailRepository.findByStatusIn(
                List.of(OrderDetail.Status.PENDING, OrderDetail.Status.COOKING, OrderDetail.Status.READY)
        );
        activeDetails.sort(Comparator.comparing(d -> d.getCreatedAt() != null ? d.getCreatedAt() : d.getOrder().getCreatedAt()));

        model.addAttribute("details", activeDetails);
        model.addAttribute("user", user);
        return "kitchen";
    }

    @PostMapping("/detail/update-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'KITCHEN')")
    public String updateKitchenDetailStatus(@RequestParam Long detailId, @RequestParam OrderDetail.Status status, org.springframework.web.servlet.mvc.support.RedirectAttributes attrs) {
        try {
            tableOrderService.updateOrderDetailStatus(detailId, status);
        } catch (IllegalStateException e) {
            attrs.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            attrs.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/kitchen";
    }
}

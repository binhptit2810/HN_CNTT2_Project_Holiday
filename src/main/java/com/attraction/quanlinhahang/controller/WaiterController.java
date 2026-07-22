package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import com.attraction.quanlinhahang.service.TableOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class WaiterController {

    @Autowired
    private TableOrderService tableOrderService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CustomerOrderRequestRepository customerOrderRequestRepository;

    @Autowired
    private CustomerOrderRequestDetailRepository customerOrderRequestDetailRepository;

    // ── QUẢN LÝ BÀN (Waiter/Admin) ───────────────────────────────

    @GetMapping("/tables")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String tablesPage(Model model) {
        User user = SecurityUtils.requireCurrentUser();
        
        model.addAttribute("tables", tableOrderService.getAllTables());
        model.addAttribute("user", user);

        // Fetch pending QR requests
        List<CustomerOrderRequest> pendingRequests = customerOrderRequestRepository.findByStatus(CustomerOrderRequest.Status.PENDING);
        model.addAttribute("pendingRequests", pendingRequests);
        
        // Map requestId -> List of details
        Map<Long, List<CustomerOrderRequestDetail>> requestDetailsMap = new HashMap<>();
        for (CustomerOrderRequest req : pendingRequests) {
            requestDetailsMap.put(req.getId(), customerOrderRequestDetailRepository.findByRequestId(req.getId()));
        }
        model.addAttribute("requestDetailsMap", requestDetailsMap);

        // Fetch all order details that are READY (finished cooking) to display to the Waiter
        List<OrderDetail> readyDetails = orderDetailRepository.findByStatus(OrderDetail.Status.READY);
        model.addAttribute("readyDetails", readyDetails);

        // Map tableNumber -> Boolean (has items preventing clear table)
        // Prevent clearing table if they have ordered any items (details not empty)
        Map<String, Boolean> tableHasItemsMap = new HashMap<>();
        List<Order> activeOrders = tableOrderService.getActiveOrders();
        for (Order order : activeOrders) {
            List<OrderDetail> details = tableOrderService.getOrderDetails(order.getId());
            if (!details.isEmpty()) {
                tableHasItemsMap.put(order.getTableNumber(), true);
            }
        }
        model.addAttribute("tableHasItemsMap", tableHasItemsMap);
        
        return "tables";
    }

    @PostMapping("/tables/detail/serve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String serveDetail(@RequestParam Long detailId, RedirectAttributes attrs) {
        try {
            tableOrderService.updateOrderDetailStatus(detailId, OrderDetail.Status.SERVED);
            attrs.addFlashAttribute("success", "Đã xác nhận phục vụ món ăn thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tables";
    }

    @PostMapping("/tables/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String openTable(@RequestParam String tableNumber, @RequestParam Integer numGuests, RedirectAttributes attrs) {
        try {
            tableOrderService.openTable(tableNumber, numGuests);
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tables";
    }

    @PostMapping("/tables/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String reserveTable(@RequestParam String tableNumber, RedirectAttributes attrs) {
        try {
            tableOrderService.reserveTable(tableNumber);
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tables";
    }

    @PostMapping("/tables/clear")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String clearTable(@RequestParam String tableNumber, RedirectAttributes attrs) {
        try {
            tableOrderService.clearTable(tableNumber);
            tableOrderService.getActiveOrderForTable(tableNumber).ifPresent(order -> {
                tableOrderService.updateOrderStatus(order.getId(), Order.Status.CANCELLED);
            });
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tables";
    }

    // ── GỌI MÓN BỞI WAITER ───────────────────────────────────────

    @GetMapping("/order/{tableNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String orderPage(@PathVariable String tableNumber, Model model) {
        User user = SecurityUtils.requireCurrentUser();

        RestaurantTable table = tableOrderService.getTableByNumber(tableNumber);
        model.addAttribute("table", table);
        model.addAttribute("menuItems", menuItemRepository.findAll());
        model.addAttribute("user", user);

        Optional<Order> activeOrder = tableOrderService.getActiveOrderForTable(tableNumber);
        if (activeOrder.isPresent()) {
            model.addAttribute("order", activeOrder.get());
            model.addAttribute("orderDetails", tableOrderService.getOrderDetails(activeOrder.get().getId()));
        } else {
            model.addAttribute("order", null);
            model.addAttribute("orderDetails", Collections.emptyList());
        }

        return "order";
    }

    @PostMapping("/order/{tableNumber}/add")
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public Map<String, Object> addOrderItems(@PathVariable String tableNumber, 
                                             @RequestBody OrderRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = SecurityUtils.requireCurrentUser();
            tableOrderService.addItemsToOrder(tableNumber, request.getItems(), request.getNotes(), user);
            response.put("success", true);
            response.put("message", "Gửi bếp thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/order/detail/update-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String updateOrderDetailStatus(@RequestParam Long detailId, 
                                          @RequestParam OrderDetail.Status status, 
                                          @RequestParam String tableNumber, 
                                          RedirectAttributes attrs) {
        try {
            tableOrderService.updateOrderDetailStatus(detailId, status);
            attrs.addFlashAttribute("success", "Cập nhật trạng thái món ăn thành công!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/order/" + tableNumber;
    }

    // ── XỬ LÝ YÊU CẦU QR ─────────────────────────────────────────

    @PostMapping("/tables/requests/{requestId}/approve")
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String approveQrRequest(@PathVariable Long requestId, RedirectAttributes attrs) {
        try {
            CustomerOrderRequest orderReq = customerOrderRequestRepository.findById(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại!"));
            
            if (orderReq.getStatus() != CustomerOrderRequest.Status.PENDING) {
                throw new IllegalStateException("Yêu cầu này đã được xử lý!");
            }

            List<CustomerOrderRequestDetail> details = customerOrderRequestDetailRepository.findByRequestId(requestId);
            
            Map<Long, Integer> items = new HashMap<>();
            Map<Long, String> notes = new HashMap<>();
            for (CustomerOrderRequestDetail d : details) {
                items.put(d.getMenuItem().getId(), d.getQuantity());
                notes.put(d.getMenuItem().getId(), d.getNote());
            }

            tableOrderService.addItemsToOrder(orderReq.getTableNumber(), items, notes, orderReq.getUser());
            
            orderReq.setStatus(CustomerOrderRequest.Status.APPROVED);
            customerOrderRequestRepository.save(orderReq);
            attrs.addFlashAttribute("success", "Đã phê duyệt yêu cầu gọi món của bàn " + orderReq.getTableNumber() + " gửi xuống bếp!");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tables";
    }

    @PostMapping("/tables/requests/{requestId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAITER')")
    public String rejectQrRequest(@PathVariable Long requestId, RedirectAttributes attrs) {
        try {
            CustomerOrderRequest orderReq = customerOrderRequestRepository.findById(requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Yêu cầu không tồn tại!"));
            orderReq.setStatus(CustomerOrderRequest.Status.REJECTED);
            customerOrderRequestRepository.save(orderReq);
            attrs.addFlashAttribute("success", "Đã từ chối yêu cầu gọi món của bàn " + orderReq.getTableNumber());
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tables";
    }
}

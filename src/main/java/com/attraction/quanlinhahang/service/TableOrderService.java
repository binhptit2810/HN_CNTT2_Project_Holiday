package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TableOrderService {

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private BillRepository billRepository;

    // Table actions
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    public RestaurantTable getTableByNumber(String tableNumber) {
        return tableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + tableNumber));
    }

    public RestaurantTable openTable(String tableNumber, int numGuests) {
        return openTable(tableNumber, numGuests, null);
    }

    public RestaurantTable openTable(String tableNumber, int numGuests, User customer) {
        RestaurantTable table = getTableByNumber(tableNumber);
        if (numGuests > table.getCapacity() * 1.5) {
            throw new IllegalArgumentException("Number of guests exceeds 1.5 times the table capacity!");
        }
        table.setStatus(RestaurantTable.Status.OCCUPIED);
        table.setCurrentCustomer(customer);
        tableRepository.save(table);

        // Auto-create active order if not exists
        List<Order> activeOrders = orderRepository.findByTableNumberAndStatusIn(
                tableNumber, Arrays.asList(Order.Status.PENDING, Order.Status.COOKING, Order.Status.READY, Order.Status.SERVED)
        );
        if (activeOrders.isEmpty()) {
            Order order = Order.builder()
                    .tableNumber(tableNumber)
                    .status(Order.Status.PENDING)
                    .build();
            orderRepository.save(order);
        }
        return table;
    }

    public RestaurantTable reserveTable(String tableNumber) {
        RestaurantTable table = getTableByNumber(tableNumber);
        table.setStatus(RestaurantTable.Status.RESERVED);
        return tableRepository.save(table);
    }

    public RestaurantTable clearTable(String tableNumber) {
        RestaurantTable table = getTableByNumber(tableNumber);
        table.setStatus(RestaurantTable.Status.EMPTY);
        table.setPaymentRequested(false);
        table.setCurrentCustomer(null);
        return tableRepository.save(table);
    }

    public RestaurantTable requestPayment(String tableNumber) {
        RestaurantTable table = getTableByNumber(tableNumber);
        table.setPaymentRequested(true);
        return tableRepository.save(table);
    }

    // Order actions
    public List<Order> getOrdersByStatus(Order.Status status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getActiveOrders() {
        List<Order> active = new ArrayList<>();
        for (Order.Status s : Arrays.asList(Order.Status.PENDING, Order.Status.COOKING, Order.Status.READY, Order.Status.SERVED)) {
            active.addAll(orderRepository.findByStatus(s));
        }
        return active;
    }

    public Optional<Order> getActiveOrderForTable(String tableNumber) {
        List<Order> activeOrders = orderRepository.findByTableNumberAndStatusIn(
                tableNumber, Arrays.asList(Order.Status.PENDING, Order.Status.COOKING, Order.Status.READY, Order.Status.SERVED)
        );
        return activeOrders.isEmpty() ? Optional.empty() : Optional.of(activeOrders.get(0));
    }

    @Transactional
    public Order addItemsToOrder(String tableNumber, Map<Long, Integer> itemsWithQuantities, Map<Long, String> itemsWithNotes, User user) {
        Order order = getActiveOrderForTable(tableNumber)
                .orElseGet(() -> {
                    // Auto-open table if it is not open
                    openTable(tableNumber, 1, user);
                    return getActiveOrderForTable(tableNumber).orElseThrow();
                });

        for (Map.Entry<Long, Integer> entry : itemsWithQuantities.entrySet()) {
            Long menuItemId = entry.getKey();
            Integer quantity = entry.getValue();
            if (quantity <= 0) continue;

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + menuItemId));
            
            if (menuItem.getStatus() == MenuItem.Status.OUT_OF_STOCK) {
                throw new IllegalArgumentException("Item is out of stock: " + menuItem.getName());
            }

            String note = itemsWithNotes != null ? itemsWithNotes.get(menuItemId) : null;

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .unitPrice(menuItem.getPrice())
                    .note(note)
                    .user(user)
                    .build();
            orderDetailRepository.save(detail);
        }

        order.setStatus(Order.Status.PENDING);
        return orderRepository.save(order);
    }

    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }

    public Order updateOrderStatus(Long orderId, Order.Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public void editOrDeleteOrderDetail(Long detailId, Integer newQuantity, boolean delete) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Mục gọi món không tồn tại: " + detailId));
        
        List<Bill> bills = billRepository.findByOrderId(detail.getOrder().getId());
        for (Bill b : bills) {
            if (b.getStatus() == Bill.Status.PAID) {
                throw new IllegalStateException("Hóa đơn của đơn hàng này đã thanh toán! Không được phép sửa đổi thông tin món ăn.");
            }
        }

        if (delete || newQuantity == null || newQuantity <= 0) {
            orderDetailRepository.delete(detail);
        } else {
            detail.setQuantity(newQuantity);
            orderDetailRepository.save(detail);
        }
    }

    @Autowired
    private InventoryService inventoryService;

    @Transactional
    public OrderDetail updateOrderDetailStatus(Long detailId, OrderDetail.Status status) {
        OrderDetail detail = orderDetailRepository.findById(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Mục gọi món không tồn tại: " + detailId));
        
        if (status == OrderDetail.Status.READY && detail.getStatus() != OrderDetail.Status.READY) {
            inventoryService.deductStockForOrderDetail(detail);
        }
        
        detail.setStatus(status);
        orderDetailRepository.save(detail);

        // Lưu ý: Hủy một OrderDetail KHÔNG tự động đánh OUT_OF_STOCK cho MenuItem.
        // Việc đánh hết hàng do checkAndUpdateMenuAvailability() quản lý.

        return detail;
    }
}

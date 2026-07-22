package com.attraction.quanlinhahang.controller;

import com.attraction.quanlinhahang.config.SecurityUtils;
import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import com.attraction.quanlinhahang.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class CustomerController {

    @Autowired
    private TableOrderService tableOrderService;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CustomerOrderRequestRepository customerOrderRequestRepository;

    @Autowired
    private CustomerOrderRequestDetailRepository customerOrderRequestDetailRepository;

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CartService cartService;

    // ── XEM THỰC ĐƠN, TÌM KIẾM & LỌC ───────────────────────────

    @GetMapping("/user/menu")
    @PreAuthorize("hasRole('USER')")
    public String userMenu(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MenuItem.Category category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(required = false) String sort,
            Model model) {
        User currentUser = SecurityUtils.requireCurrentUser();

        List<MenuItem> items = new ArrayList<>(menuItemRepository.findAll());

        if (search != null && !search.trim().isEmpty()) {
            String q = search.toLowerCase();
            items.removeIf(i -> !i.getName().toLowerCase().contains(q));
        }
        if (category != null) {
            items.removeIf(i -> i.getCategory() != category);
        }
        if (minPrice != null) {
            items.removeIf(i -> i.getPrice() < minPrice);
        }
        if (maxPrice != null) {
            items.removeIf(i -> i.getPrice() > maxPrice);
        }
        if (availableOnly != null && availableOnly) {
            items.removeIf(i -> i.getStatus() != MenuItem.Status.AVAILABLE);
        }
        if (sort != null) {
            if (sort.equalsIgnoreCase("priceAsc")) {
                items.sort(Comparator.comparing(MenuItem::getPrice));
            } else if (sort.equalsIgnoreCase("priceDesc")) {
                items.sort(Comparator.comparing(MenuItem::getPrice).reversed());
            }
        }

        List<MenuItem> favorites = favoriteService.getFavorites(currentUser.getId());
        Set<Long> favIds = new HashSet<>();
        for (MenuItem f : favorites) {
            favIds.add(f.getId());
        }

        model.addAttribute("menuItems", items);
        model.addAttribute("favIds", favIds);
        model.addAttribute("user", currentUser);
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("availableOnly", availableOnly);
        model.addAttribute("sort", sort);
        return "user/menu";
    }

    // ── YÊU THÍCH ──────────────────────────────────────────────

    @PostMapping("/user/favorites/toggle")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> toggleFavorite(@RequestParam Long menuItemId) {
        Map<String, Object> res = new HashMap<>();
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            boolean isFav = favoriteService.toggleFavorite(currentUser.getId(), menuItemId);
            res.put("success", true);
            res.put("isFavorite", isFav);
            res.put("message", isFav ? "Đã thêm vào danh sách yêu thích!" : "Đã xoá khỏi danh sách yêu thích!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @GetMapping("/user/favorites")
    @PreAuthorize("hasRole('USER')")
    public String viewFavorites(Model model) {
        User currentUser = SecurityUtils.requireCurrentUser();
        List<MenuItem> items = favoriteService.getFavorites(currentUser.getId());
        model.addAttribute("menuItems", items);
        model.addAttribute("user", currentUser);
        return "user/favorites";
    }

    // ── GIỎ HÀNG ───────────────────────────────────────────────

    @GetMapping("/user/cart")
    @PreAuthorize("hasRole('USER')")
    public String viewCart(Model model) {
        User currentUser = SecurityUtils.requireCurrentUser();

        List<CartItem> cartItems = cartService.getCartItems(currentUser.getId());
        Double total = cartService.getCartTotal(currentUser.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("user", currentUser);
        model.addAttribute("tables", tableOrderService.getAllTables());
        return "user/cart";
    }

    @PostMapping("/user/cart/checkout")
    @ResponseBody
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> checkoutCart(@RequestParam String tableNumber) {
        Map<String, Object> res = new HashMap<>();
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            List<CartItem> cartItems = cartService.getCartItems(currentUser.getId());
            if (cartItems.isEmpty()) {
                res.put("success", false);
                res.put("message", "Giỏ hàng của bạn đang trống!");
                return res;
            }

            RestaurantTable table = tableOrderService.getTableByNumber(tableNumber);

            CustomerOrderRequest orderReq = CustomerOrderRequest.builder()
                    .tableNumber(tableNumber)
                    .status(CustomerOrderRequest.Status.PENDING)
                    .user(currentUser)
                    .build();
            customerOrderRequestRepository.save(orderReq);

            for (CartItem item : cartItems) {
                if (item.getMenuItem().getStatus() == MenuItem.Status.OUT_OF_STOCK) {
                    throw new IllegalArgumentException("Món ăn '" + item.getMenuItem().getName() + "' đã tạm hết hàng!");
                }
                CustomerOrderRequestDetail detail = CustomerOrderRequestDetail.builder()
                        .request(orderReq)
                        .menuItem(item.getMenuItem())
                        .quantity(item.getQuantity())
                        .note(item.getNote())
                        .build();
                customerOrderRequestDetailRepository.save(detail);
            }

            if (table.getStatus() != RestaurantTable.Status.EMPTY) {
                if (table.getCurrentCustomer() == null || !table.getCurrentCustomer().getId().equals(currentUser.getId())) {
                    throw new IllegalArgumentException("Bàn này đã được tài khoản khác sử dụng. Vui lòng chọn bàn khác!");
                }
            }

            if (table.getStatus() == RestaurantTable.Status.EMPTY) {
                tableOrderService.openTable(tableNumber, 1, currentUser);
            }

            cartService.clearCart(currentUser.getId());

            res.put("success", true);
            res.put("tableNumber", tableNumber);
            res.put("message", "Đặt món thành công! Yêu cầu gọi món đã được gửi tới nhân viên.");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/user/cart/add")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> addToCart(
            @RequestParam Long menuItemId,
            @RequestParam(defaultValue = "1") Integer quantity,
            @RequestParam(required = false) String note) {
        Map<String, Object> res = new HashMap<>();
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            cartService.addItem(currentUser.getId(), menuItemId, quantity, note);
            res.put("success", true);
            res.put("message", "Đã thêm món vào giỏ hàng thành công!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/user/cart/update-qty")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> updateCartQty(
            @RequestParam Long menuItemId,
            @RequestParam Integer delta) {
        Map<String, Object> res = new HashMap<>();
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            cartService.updateQty(currentUser.getId(), menuItemId, delta);
            res.put("success", true);
            res.put("total", cartService.getCartTotal(currentUser.getId()));
            res.put("message", "Cập nhật số lượng thành công!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("/user/cart/remove")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> removeFromCart(@RequestParam Long menuItemId) {
        Map<String, Object> res = new HashMap<>();
        User currentUser = SecurityUtils.requireCurrentUser();
        try {
            cartService.removeItem(currentUser.getId(), menuItemId);
            res.put("success", true);
            res.put("total", cartService.getCartTotal(currentUser.getId()));
            res.put("message", "Đã xoá món khỏi giỏ hàng!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return res;
    }

    // ── KHÁCH HÀNG – GỌI MÓN TRỰC TIẾP TẠI BÀN ───────────────────

    @GetMapping("/customer/select-table")
    @PreAuthorize("hasRole('USER')")
    public String selectCustomerTable(Model model) {
        User user = SecurityUtils.requireCurrentUser();
        model.addAttribute("tables", tableOrderService.getAllTables());
        model.addAttribute("user", user);
        return "customer/select-table";
    }

    @GetMapping("/customer/order/{tableNumber}")
    @PreAuthorize("hasRole('USER')")
    public String customerOrderPage(@PathVariable String tableNumber, Model model) {
        User user = SecurityUtils.requireCurrentUser();
        RestaurantTable table = tableOrderService.getTableByNumber(tableNumber);

        if (table.getStatus() == RestaurantTable.Status.OCCUPIED) {
            if (table.getCurrentCustomer() == null || !table.getCurrentCustomer().getId().equals(user.getId())) {
                return "redirect:/customer/select-table?error=occupied";
            }
        }

        model.addAttribute("table", table);
        model.addAttribute("menuItems", menuItemRepository.findAll());
        model.addAttribute("isUserDirect", true);
        model.addAttribute("user", user);

        Optional<Order> activeOrder = tableOrderService.getActiveOrderForTable(tableNumber);
        if (activeOrder.isPresent()) {
            model.addAttribute("order", activeOrder.get());
            List<OrderDetail> allDetails = tableOrderService.getOrderDetails(activeOrder.get().getId());
            List<OrderDetail> myDetails = allDetails.stream()
                    .filter(d -> d.getUser() != null && d.getUser().getId().equals(user.getId()))
                    .toList();
            model.addAttribute("orderDetails", myDetails);
        } else {
            model.addAttribute("order", null);
            model.addAttribute("orderDetails", Collections.emptyList());
        }

        return "qr-order";
    }

    @PostMapping("/customer/order/{tableNumber}/submit")
    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> submitDirectCustomerOrder(@PathVariable String tableNumber, 
                                                         @RequestBody OrderRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = SecurityUtils.requireCurrentUser();
            RestaurantTable table = tableOrderService.getTableByNumber(tableNumber);
            
            if (table.getStatus() == RestaurantTable.Status.OCCUPIED) {
                if (table.getCurrentCustomer() == null || !table.getCurrentCustomer().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("Bàn này đã được tài khoản khác sử dụng!");
                }
            }
            
            tableOrderService.addItemsToOrder(tableNumber, request.getItems(), request.getNotes(), user);
            response.put("success", true);
            response.put("message", "Gọi món trực tuyến thành công! Các món ăn đã được gửi thẳng xuống bếp chế biến.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/customer/order/{tableNumber}/request-payment")
    @PreAuthorize("hasRole('USER')")
    public String requestCustomerPayment(@PathVariable String tableNumber, RedirectAttributes attrs) {
        User user = SecurityUtils.requireCurrentUser();
        try {
            Optional<Order> activeOrder = tableOrderService.getActiveOrderForTable(tableNumber);
            if (activeOrder.isEmpty()) {
                throw new IllegalStateException("Bàn này hiện không có hóa đơn hoạt động!");
            }
            List<OrderDetail> allDetails = tableOrderService.getOrderDetails(activeOrder.get().getId());
            boolean hasOrdered = allDetails.stream()
                    .anyMatch(d -> d.getUser() != null && d.getUser().getId().equals(user.getId()));
            
            if (!hasOrdered) {
                throw new IllegalStateException("Bạn chưa đặt món ăn nào tại bàn này nên không thể yêu cầu thanh toán!");
            }

            tableOrderService.requestPayment(tableNumber);
            attrs.addFlashAttribute("success", "Đã gửi yêu cầu thanh toán thành công! Vui lòng đợi nhân viên phục vụ trong giây lát.");
        } catch (Exception e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/order/" + tableNumber;
    }

    // ── GỌI MÓN QUA QR (PUBLIC - KHÔNG CẦN LOGIN) ────────────────

    @GetMapping("/qr/order/{tableNumber}")
    public String qrOrderPage(@PathVariable String tableNumber, Model model) {
        RestaurantTable table = tableOrderService.getTableByNumber(tableNumber);
        model.addAttribute("table", table);
        model.addAttribute("menuItems", menuItemRepository.findAll());
        return "qr-order";
    }

    @PostMapping("/qr/order/{tableNumber}/request")
    @ResponseBody
    @Transactional
    public Map<String, Object> submitQrRequest(@PathVariable String tableNumber, @RequestBody OrderRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        try {
            CustomerOrderRequest orderReq = CustomerOrderRequest.builder()
                    .tableNumber(tableNumber)
                    .status(CustomerOrderRequest.Status.PENDING)
                    .build();
            customerOrderRequestRepository.save(orderReq);

            for (Map.Entry<Long, Integer> entry : request.getItems().entrySet()) {
                Long menuItemId = entry.getKey();
                Integer qty = entry.getValue();
                if (qty <= 0) continue;

                MenuItem item = menuItemRepository.findById(menuItemId).orElseThrow();
                
                if (item.getStatus() == MenuItem.Status.OUT_OF_STOCK) {
                    throw new IllegalArgumentException("Món ăn '" + item.getName() + "' đã hết hàng!");
                }

                CustomerOrderRequestDetail detail = CustomerOrderRequestDetail.builder()
                        .request(orderReq)
                        .menuItem(item)
                        .quantity(qty)
                        .note(request.getNotes() != null ? request.getNotes().get(menuItemId) : null)
                        .build();
                customerOrderRequestDetailRepository.save(detail);
            }

            response.put("success", true);
            response.put("message", "Gửi yêu cầu thành công! Vui lòng đợi nhân viên phục vụ phê duyệt.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}

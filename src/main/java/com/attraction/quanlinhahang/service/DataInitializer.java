package com.attraction.quanlinhahang.service;

import com.attraction.quanlinhahang.model.*;
import com.attraction.quanlinhahang.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Users
        if (userRepository.count() == 0) {
            userService.createUser("admin", "admin", User.Role.ADMIN);
            userService.createUser("waiter", "waiter", User.Role.WAITER);
            userService.createUser("cashier", "cashier", User.Role.CASHIER);
            userService.createUser("chef", "chef", User.Role.KITCHEN);
        } else {
            // Đảm bảo mật khẩu của các user cũ đã được mã hóa BCrypt
            List<User> users = userRepository.findAll();
            boolean updated = false;
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            for (User u : users) {
                // Nếu mật khẩu không bắt đầu bằng $2a$ (dấu hiệu của BCrypt), ta sẽ mã hóa lại
                if (u.getPassword() != null && !u.getPassword().startsWith("$2a$")) {
                    u.setPassword(encoder.encode(u.getPassword()));
                    updated = true;
                }
            }
            if (updated) {
                userRepository.saveAll(users);
            }
        }

        // 2. Initialize Tables
        if (tableRepository.count() == 0) {
            tableRepository.saveAll(Arrays.asList(
                RestaurantTable.builder().tableNumber("101").capacity(4).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("102").capacity(4).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("103").capacity(2).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("104").capacity(6).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("105").capacity(8).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("201").capacity(4).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("202").capacity(4).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("203").capacity(2).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("204").capacity(6).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("VIP01").capacity(10).status(RestaurantTable.Status.EMPTY).build(),
                RestaurantTable.builder().tableNumber("VIP02").capacity(12).status(RestaurantTable.Status.EMPTY).build()
            ));
        }

        // 3. Initialize Ingredients
        if (ingredientRepository.count() == 0) {
            ingredientRepository.saveAll(Arrays.asList(
                Ingredient.builder().name("Thịt bò phi lê").quantityInStock(50.0).unit("kg").reorderLevel(10.0).build(),
                Ingredient.builder().name("Hải sản tổng hợp").quantityInStock(30.0).unit("kg").reorderLevel(5.0).build(),
                Ingredient.builder().name("Gạo thơm").quantityInStock(100.0).unit("kg").reorderLevel(20.0).build(),
                Ingredient.builder().name("Rau lẩu tổng hợp").quantityInStock(20.0).unit("kg").reorderLevel(5.0).build(),
                Ingredient.builder().name("Cam tươi").quantityInStock(40.0).unit("kg").reorderLevel(10.0).build()
            ));
        }

        // 4. Initialize Menu Items
        if (menuItemRepository.count() == 0) {
            MenuItem sup = MenuItem.builder()
                    .name("Súp hải sản")
                    .price(45000.0)
                    .category(MenuItem.Category.APPETIZER)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Súp hải sản nóng hổi thơm ngậy với ghẹ, tôm, ngô ngọt.")
                    .imageUrl("https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem salad = MenuItem.builder()
                    .name("Salad hoàng đế")
                    .price(55000.0)
                    .category(MenuItem.Category.APPETIZER)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Salad rau xanh giòn rụm kèm sốt Cesar, phô mai bột, bánh mì sấy.")
                    .imageUrl("https://images.unsplash.com/photo-1550304943-4f24f54ddde9?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem lau = MenuItem.builder()
                    .name("Lẩu Thái hải sản")
                    .price(299000.0)
                    .category(MenuItem.Category.MAIN)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Lẩu Thái chua cay đậm vị phục vụ kèm tôm, mực, bò phi lê và rau nấm.")
                    .imageUrl("https://images.unsplash.com/photo-1555126634-323283e090fa?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem bo = MenuItem.builder()
                    .name("Bò sốt tiêu đen")
                    .price(185000.0)
                    .category(MenuItem.Category.MAIN)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Bò xào bản gang sốt tiêu đen kèm hành tây và ớt chuông.")
                    .imageUrl("https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem com = MenuItem.builder()
                    .name("Cơm chiên Dương Châu")
                    .price(75000.0)
                    .category(MenuItem.Category.MAIN)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Cơm chiên tơi xốp hạt kèm chả lụa, lạp xưởng, đậu hà lan.")
                    .imageUrl("https://images.unsplash.com/photo-1623595110708-76b2af83d76b?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem bia = MenuItem.builder()
                    .name("Bia Heineken")
                    .price(25000.0)
                    .category(MenuItem.Category.DRINK)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Bia lon ướp lạnh mát lạnh.")
                    .imageUrl("https://images.unsplash.com/photo-1600788886242-5c96aabe3757?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem cam = MenuItem.builder()
                    .name("Nước cam ép")
                    .price(35000.0)
                    .category(MenuItem.Category.DRINK)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Cam tươi nguyên chất vắt tay tại chỗ.")
                    .imageUrl("https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?q=80&w=600&auto=format&fit=crop")
                    .build();

            MenuItem che = MenuItem.builder()
                    .name("Chè khoai dẻo")
                    .price(30000.0)
                    .category(MenuItem.Category.DESSERT)
                    .status(MenuItem.Status.AVAILABLE)
                    .description("Chè khoai ngọt bùi kết hợp cốt dừa béo ngậy.")
                    .imageUrl("https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?q=80&w=600&auto=format&fit=crop")
                    .build();

            menuItemRepository.saveAll(Arrays.asList(sup, salad, lau, bo, com, bia, cam, che));

            // Seed Recipes for Menu Items
            Ingredient boFile = ingredientRepository.findByName("Thịt bò phi lê").orElse(null);
            Ingredient haiSan = ingredientRepository.findByName("Hải sản tổng hợp").orElse(null);
            Ingredient gao = ingredientRepository.findByName("Gạo thơm").orElse(null);
            Ingredient rauLau = ingredientRepository.findByName("Rau lẩu tổng hợp").orElse(null);
            Ingredient camTuoi = ingredientRepository.findByName("Cam tươi").orElse(null);

            if (haiSan != null && rauLau != null) {
                inventoryService.saveRecipe(lau, haiSan, 0.5);
                inventoryService.saveRecipe(lau, rauLau, 0.4);
            }
            if (boFile != null) {
                inventoryService.saveRecipe(bo, boFile, 0.25);
            }
            if (gao != null) {
                inventoryService.saveRecipe(com, gao, 0.2);
            }
            if (camTuoi != null) {
                inventoryService.saveRecipe(cam, camTuoi, 0.3); // 0.3 kg cam / ly
            }
        }
        updateRealImages();

        // [MIGRATION] Fix ENUM truncation errors
        System.out.println("====== CẬP NHẬT CẤU TRÚC DATABASE (VARCHAR ENUMS) ======");
        try {
            String[] alterQueries = {
                "ALTER TABLE orders MODIFY COLUMN status VARCHAR(50);",
                "ALTER TABLE bills MODIFY COLUMN status VARCHAR(50);",
                "ALTER TABLE bills MODIFY COLUMN payment_method VARCHAR(50);",
                "ALTER TABLE menu_items MODIFY COLUMN category VARCHAR(50);",
                "ALTER TABLE menu_items MODIFY COLUMN status VARCHAR(50);",
                "ALTER TABLE restaurant_tables MODIFY COLUMN status VARCHAR(50);",
                "ALTER TABLE users MODIFY COLUMN role VARCHAR(50);",
                "ALTER TABLE customer_order_requests MODIFY COLUMN status VARCHAR(50);"
            };
            for (String q : alterQueries) {
                try {
                    jdbcTemplate.execute(q);
                } catch (Exception e) {}
            }
            System.out.println("====== CẬP NHẬT CẤU TRÚC THÀNH CÔNG ======");
        } catch (Exception e) {
            System.out.println("Lỗi chung khi cập nhật cấu trúc: " + e.getMessage());
        }

    }

    private void updateRealImages() {
        updateItemImage("Súp hải sản", "https://images.unsplash.com/photo-1547592180-85f173990554?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Salad hoàng đế", "https://images.unsplash.com/photo-1550304943-4f24f54ddde9?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Lẩu Thái hải sản", "https://images.unsplash.com/photo-1555126634-323283e090fa?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Bò sốt tiêu đen", "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Cơm chiên Dương Châu", "https://images.unsplash.com/photo-1623595110708-76b2af83d76b?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Bia Heineken", "https://images.unsplash.com/photo-1600788886242-5c96aabe3757?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Nước cam ép", "https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?q=80&w=600&auto=format&fit=crop");
        updateItemImage("Chè khoai dẻo", "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?q=80&w=600&auto=format&fit=crop");
    }

    private void updateItemImage(String name, String imageUrl) {
        menuItemRepository.findByName(name).ifPresent(item -> {
            if (item.getImageUrl() == null || item.getImageUrl().trim().isEmpty() || item.getImageUrl().contains("placehold.co") || !item.getImageUrl().equals(imageUrl)) {
                item.setImageUrl(imageUrl);
                menuItemRepository.save(item);
            }
        });
    }
}

package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(50)") // Khai báo VARCHAR để tương thích lệnh ALTER TABLE của MySQL
    private Status status;

    @Column(name = "created_at") 
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.PENDING;
        }
    }

    // Đảm bảo Null-Safety khi nạp dữ liệu cũ
    public Status getStatus() {
        return this.status == null ? Status.PENDING : this.status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt == null ? (this.order != null ? this.order.getCreatedAt() : LocalDateTime.now()) : this.createdAt;
    }

    public enum Status {
        PENDING, COOKING, READY, SERVED, CANCELLED
    }
}

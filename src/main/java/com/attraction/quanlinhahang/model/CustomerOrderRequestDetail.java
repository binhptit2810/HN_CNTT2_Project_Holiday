package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_order_request_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrderRequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private CustomerOrderRequest request;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity;

    private String note;
}

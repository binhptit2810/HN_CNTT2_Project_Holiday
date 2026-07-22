package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Builder.Default
    @Column(name = "payment_requested", nullable = false)
    private Boolean paymentRequested = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_customer_id")
    private User currentCustomer;

    public enum Status {
        EMPTY, OCCUPIED, RESERVED
    }
}

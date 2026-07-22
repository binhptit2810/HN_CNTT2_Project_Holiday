package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", nullable = false, unique = true)
    private String name;

    @Column(name = "quantity_in_stock", nullable = false)
    private Double quantityInStock;

    @Column(nullable = false)
    private String unit;

    @Column(name = "reorder_level", nullable = false)
    private Double reorderLevel;
}

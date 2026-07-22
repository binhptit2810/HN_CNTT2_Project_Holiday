package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_item_recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemRecipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "quantity_required", nullable = false)
    private Double quantityRequired;
}

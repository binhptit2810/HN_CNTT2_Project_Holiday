package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private Role role;

    // ── Thông tin cá nhân ──────────────────────────────────────
    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "lock_reason")
    private String lockReason;

    public enum Role {
        ADMIN, WAITER, CASHIER, KITCHEN, USER
    }
}

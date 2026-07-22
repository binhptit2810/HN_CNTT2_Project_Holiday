package com.attraction.quanlinhahang.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "start_balance", nullable = false)
    private Double startBalance;

    @Column(name = "end_balance_declared")
    private Double endBalanceDeclared;

    @Column(name = "end_balance_calculated")
    private Double endBalanceCalculated;

    @Column(name = "difference")
    private Double difference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum Status {
        OPEN, CLOSED
    }
}

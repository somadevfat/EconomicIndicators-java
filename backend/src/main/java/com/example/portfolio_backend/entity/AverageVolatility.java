package com.example.portfolio_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "average_volatilities")
public class AverageVolatility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "indicator_name", length = 100, nullable = false)
    private String indicatorName;

    @Column(length = 2, nullable = false)
    private String country;

    @Column(length = 6)
    private String pair;

    @Column(name = "time_slot", length = 5)
    private String timeSlot;

    @Column(name = "average_value", nullable = false)
    private Double averageValue;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @PrePersist
    protected void onCreate() {
        calculatedAt = LocalDateTime.now();
    }
} 
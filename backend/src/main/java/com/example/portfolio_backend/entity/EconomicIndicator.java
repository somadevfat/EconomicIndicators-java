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
@Table(name = "economic_indicators")
public class EconomicIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utc_time", nullable = false)
    private LocalDateTime utcTime;

    @Column(name = "jst_time", nullable = false)
    private LocalDateTime jstTime;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "country", length = 2, nullable = false)
    private String country;

    private Double actual;
    private Double forecast;
    private Double previous;

    @OneToOne
    @JoinColumn(name = "linked_volatility_id")
    private VolatilityData linkedVolatility;

    @Column(name = "market_condition")
    private String marketCondition;

    @Column(name = "sentiment")
    private String sentiment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 
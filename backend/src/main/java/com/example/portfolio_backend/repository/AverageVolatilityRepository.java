package com.example.portfolio_backend.repository;

import com.example.portfolio_backend.entity.AverageVolatility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AverageVolatilityRepository extends JpaRepository<AverageVolatility, Long> {
} 
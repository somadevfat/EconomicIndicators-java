package com.example.portfolio_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio_backend.entity.AverageVolatility;
 
@Repository
public interface AverageVolatilityRepository extends JpaRepository<AverageVolatility, Long> {
} 
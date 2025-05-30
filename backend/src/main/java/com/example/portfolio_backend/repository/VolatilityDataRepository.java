package com.example.portfolio_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio_backend.entity.VolatilityData;
 
@Repository
public interface VolatilityDataRepository extends JpaRepository<VolatilityData, Long> {
} 
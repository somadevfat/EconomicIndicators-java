package com.example.portfolio_backend.repository;

import com.example.portfolio_backend.entity.VolatilityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolatilityDataRepository extends JpaRepository<VolatilityData, Long> {
} 
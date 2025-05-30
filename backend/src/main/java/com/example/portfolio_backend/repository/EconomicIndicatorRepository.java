package com.example.portfolio_backend.repository;

import com.example.portfolio_backend.entity.EconomicIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {
} 
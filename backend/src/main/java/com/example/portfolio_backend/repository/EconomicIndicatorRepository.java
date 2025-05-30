package com.example.portfolio_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio_backend.entity.EconomicIndicator;
 
@Repository
public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {
} 
package com.example.portfolio_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.portfolio_backend.entity.EconomicIndicator;
 
public interface EconomicIndicatorService {
    void importIndicators(List<EconomicIndicator> indicators);
    List<EconomicIndicator> getAllIndicators();
    List<EconomicIndicator> getRecentIndicators(String name, LocalDateTime jstTime);
    Optional<EconomicIndicator> getIndicatorById(Long id);
    EconomicIndicator saveIndicator(EconomicIndicator indicator);
    void deleteIndicator(Long id);
} 
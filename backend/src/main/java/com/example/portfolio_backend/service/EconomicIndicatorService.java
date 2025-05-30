package com.example.portfolio_backend.service;

import java.util.List;

import com.example.portfolio_backend.entity.EconomicIndicator;
 
public interface EconomicIndicatorService {
    void importIndicators(List<EconomicIndicator> indicators);
} 
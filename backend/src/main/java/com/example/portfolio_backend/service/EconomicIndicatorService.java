package com.example.portfolio_backend.service;

import com.example.portfolio_backend.entity.EconomicIndicator;
import java.util.List;

public interface EconomicIndicatorService {
    void importIndicators(List<EconomicIndicator> indicators);
} 
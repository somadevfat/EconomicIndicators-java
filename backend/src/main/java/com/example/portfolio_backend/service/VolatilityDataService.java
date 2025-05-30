package com.example.portfolio_backend.service;

import java.util.List;

import com.example.portfolio_backend.entity.VolatilityData;
 
public interface VolatilityDataService {
    void importVolatility(List<VolatilityData> data);
} 
package com.example.portfolio_backend.service;

import java.util.List;
import java.util.Optional;

import com.example.portfolio_backend.entity.VolatilityData;
 
public interface VolatilityDataService {
    void importVolatility(List<VolatilityData> data);
    List<VolatilityData> getAllVolatility();
    Optional<VolatilityData> getVolatilityDataById(Long id);
    VolatilityData saveVolatilityData(VolatilityData volatilityData);
    void deleteVolatilityData(Long id);
} 
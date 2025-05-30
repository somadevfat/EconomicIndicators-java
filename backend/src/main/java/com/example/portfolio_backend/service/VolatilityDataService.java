package com.example.portfolio_backend.service;

import com.example.portfolio_backend.entity.VolatilityData;
import java.util.List;

public interface VolatilityDataService {
    void importVolatility(List<VolatilityData> data);
} 
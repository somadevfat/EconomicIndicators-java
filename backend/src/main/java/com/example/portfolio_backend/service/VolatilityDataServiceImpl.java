package com.example.portfolio_backend.service;

import com.example.portfolio_backend.entity.VolatilityData;
import com.example.portfolio_backend.repository.VolatilityDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VolatilityDataServiceImpl implements VolatilityDataService {

    private final VolatilityDataRepository repository;

    public VolatilityDataServiceImpl(VolatilityDataRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void importVolatility(List<VolatilityData> data) {
        for (VolatilityData entry : data) {
            repository.save(entry);
        }
    }
} 
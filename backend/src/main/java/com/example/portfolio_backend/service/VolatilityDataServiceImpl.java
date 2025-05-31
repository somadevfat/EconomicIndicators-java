package com.example.portfolio_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.portfolio_backend.entity.VolatilityData;
import com.example.portfolio_backend.repository.VolatilityDataRepository;

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

    @Override
    public List<VolatilityData> getAllVolatility() {
        return repository.findAll();
    }
} 
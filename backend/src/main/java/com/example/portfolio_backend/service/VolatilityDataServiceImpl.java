package com.example.portfolio_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.portfolio_backend.entity.VolatilityData;
import com.example.portfolio_backend.repository.VolatilityDataRepository;

@Service
public class VolatilityDataServiceImpl implements VolatilityDataService {

    private final VolatilityDataRepository volatilityDataRepository;

    public VolatilityDataServiceImpl(VolatilityDataRepository volatilityDataRepository) {
        this.volatilityDataRepository = volatilityDataRepository;
    }

    @Override
    @Transactional
    public void importVolatility(List<VolatilityData> data) {
        for (VolatilityData entry : data) {
            volatilityDataRepository.save(entry);
        }
    }

    @Override
    public List<VolatilityData> getAllVolatility() {
        return volatilityDataRepository.findAll();
    }

    @Override
    public Optional<VolatilityData> getVolatilityDataById(Long id) {
        return volatilityDataRepository.findById(id);
    }

    @Override
    @Transactional
    public VolatilityData saveVolatilityData(VolatilityData volatilityData) {
        return volatilityDataRepository.save(volatilityData);
    }

    @Override
    @Transactional
    public void deleteVolatilityData(Long id) {
        volatilityDataRepository.deleteById(id);
    }
} 
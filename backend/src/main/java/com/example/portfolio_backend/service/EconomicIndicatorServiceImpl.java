package com.example.portfolio_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.repository.EconomicIndicatorRepository;

@Service
public class EconomicIndicatorServiceImpl implements EconomicIndicatorService {

    private final EconomicIndicatorRepository repository;

    public EconomicIndicatorServiceImpl(EconomicIndicatorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void importIndicators(List<EconomicIndicator> indicators) {
        for (EconomicIndicator indicator : indicators) {
            LocalDateTime utc = indicator.getUtcTime();
            if (utc != null) {
                indicator.setJstTime(utc.plusHours(9));
            }
            repository.save(indicator);
        }
    }

    @Override
    public List<EconomicIndicator> getAllIndicators() {
        return repository.findAll();
    }
} 
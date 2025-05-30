package com.example.portfolio_backend.controller;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.service.EconomicIndicatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/indicators")
public class EconomicIndicatorController {

    private final EconomicIndicatorService economicIndicatorService;

    public EconomicIndicatorController(EconomicIndicatorService economicIndicatorService) {
        this.economicIndicatorService = economicIndicatorService;
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importIndicators(@RequestBody List<EconomicIndicator> indicators) {
        economicIndicatorService.importIndicators(indicators);
        return ResponseEntity.ok().build();
    }
} 
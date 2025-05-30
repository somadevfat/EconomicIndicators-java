package com.example.portfolio_backend.controller;

import com.example.portfolio_backend.entity.VolatilityData;
import com.example.portfolio_backend.service.VolatilityDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/volatility")
public class VolatilityDataController {

    private final VolatilityDataService volatilityDataService;

    public VolatilityDataController(VolatilityDataService volatilityDataService) {
        this.volatilityDataService = volatilityDataService;
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importVolatility(@RequestBody List<VolatilityData> data) {
        volatilityDataService.importVolatility(data);
        return ResponseEntity.ok().build();
    }
} 
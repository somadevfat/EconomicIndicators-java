package com.example.portfolio_backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.entity.VolatilityData;

@RestController
@RequestMapping("/api/mock")
public class MockController {

    // モックデータを返すコントローラー（DB接続なし）

    @GetMapping("/indicators")
    public ResponseEntity<List<EconomicIndicator>> getMockIndicators() {
        List<EconomicIndicator> list = new ArrayList<>();
        LocalDateTime utc1 = LocalDateTime.of(2023, Month.JANUARY, 1, 0, 0);
        EconomicIndicator indicator1 = new EconomicIndicator();
        indicator1.setUtcTime(utc1);
        indicator1.setJstTime(utc1.plusHours(9));
        indicator1.setName("GDP");
        indicator1.setCountry("US");
        indicator1.setActual(1.2);
        indicator1.setForecast(1.0);
        indicator1.setPrevious(1.1);
        list.add(indicator1);
        LocalDateTime utc2 = LocalDateTime.of(2023, Month.JANUARY, 1, 1, 0);
        EconomicIndicator indicator2 = new EconomicIndicator();
        indicator2.setUtcTime(utc2);
        indicator2.setJstTime(utc2.plusHours(9));
        indicator2.setName("CPI");
        indicator2.setCountry("US");
        indicator2.setActual(0.5);
        indicator2.setForecast(0.4);
        indicator2.setPrevious(0.5);
        list.add(indicator2);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/volatility")
    public ResponseEntity<List<VolatilityData>> getMockVolatility() {
        List<VolatilityData> list = new ArrayList<>();
        VolatilityData vol1 = new VolatilityData();
        vol1.setDate(LocalDate.of(2023, Month.JANUARY, 1));
        vol1.setTimeSlot("00:00");
        vol1.setPair("USDJPY");
        vol1.setHigh(100.0);
        vol1.setLow(99.5);
        vol1.setVolatility(0.5);
        list.add(vol1);
        VolatilityData vol2 = new VolatilityData();
        vol2.setDate(LocalDate.of(2023, Month.JANUARY, 1));
        vol2.setTimeSlot("01:00");
        vol2.setPair("USDJPY");
        vol2.setHigh(100.5);
        vol2.setLow(100.0);
        vol2.setVolatility(0.5);
        list.add(vol2);
        return ResponseEntity.ok(list);
    }
} 
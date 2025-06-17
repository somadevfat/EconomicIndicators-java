package com.example.portfolio_backend.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.portfolio_backend.dto.AnalysisSummaryDto;
import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.service.EconomicIndicatorService;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final EconomicIndicatorService economicIndicatorService;

    public AnalysisController(EconomicIndicatorService economicIndicatorService) {
        this.economicIndicatorService = economicIndicatorService;
    }

    // GET /api/analysis/summary?indicatorId={id} : 特定指標の分析サマリー
    // GET /api/analysis/summary                 : 全指標の分析サマリーリスト
    @GetMapping("/summary")
    public ResponseEntity<?> getAnalysisSummary(@RequestParam(required = false) Long indicatorId) {
        if (indicatorId != null) {
            Optional<EconomicIndicator> currentIndicatorOpt = economicIndicatorService.getIndicatorById(indicatorId);
            if (currentIndicatorOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            EconomicIndicator currentIndicator = currentIndicatorOpt.get();
            List<EconomicIndicator> recentIndicators = economicIndicatorService.getRecentIndicators(currentIndicator.getName(), currentIndicator.getJstTime());
            AnalysisSummaryDto summary = new AnalysisSummaryDto(currentIndicator, recentIndicators);
            return ResponseEntity.ok(summary);
        } else {
            List<EconomicIndicator> allIndicators = economicIndicatorService.getAllIndicators();
            if (allIndicators.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<AnalysisSummaryDto> summaries = allIndicators.stream().map(indicator -> {
                List<EconomicIndicator> recent = economicIndicatorService.getRecentIndicators(indicator.getName(), indicator.getJstTime());
                return new AnalysisSummaryDto(indicator, recent);
            }).collect(Collectors.toList());
            return ResponseEntity.ok(summaries);
        }
    }
} 
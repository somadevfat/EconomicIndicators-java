package com.example.portfolio_backend.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.entity.VolatilityData;
import com.example.portfolio_backend.repository.EconomicIndicatorRepository;
import com.example.portfolio_backend.service.analysis.IndicatorDirection;
import com.example.portfolio_backend.service.analysis.MarketAnalysisService;
import com.example.portfolio_backend.service.analysis.MarketCondition;
import com.example.portfolio_backend.service.analysis.MarketSentiment;

@Service
public class EconomicIndicatorServiceImpl implements EconomicIndicatorService {

    private final EconomicIndicatorRepository economicIndicatorRepository;
    private final VolatilityLinkingService volatilityLinkingService;
    private final VolatilityDataService volatilityDataService;
    private final MarketAnalysisService marketAnalysisService;

    private static final Map<String, IndicatorDirection> INDICATOR_DIRECTIONS = new HashMap<>();
    static {
        INDICATOR_DIRECTIONS.put("Non-Farm Payrolls", IndicatorDirection.POSITIVE);
        INDICATOR_DIRECTIONS.put("Unemployment Rate", IndicatorDirection.NEGATIVE);
        INDICATOR_DIRECTIONS.put("GDP", IndicatorDirection.POSITIVE);
        INDICATOR_DIRECTIONS.put("CPI", IndicatorDirection.NEGATIVE);
    }

    public EconomicIndicatorServiceImpl(EconomicIndicatorRepository economicIndicatorRepository, 
                                        VolatilityLinkingService volatilityLinkingService,
                                        VolatilityDataService volatilityDataService,
                                        MarketAnalysisService marketAnalysisService) {
        this.economicIndicatorRepository = economicIndicatorRepository;
        this.volatilityLinkingService = volatilityLinkingService;
        this.volatilityDataService = volatilityDataService;
        this.marketAnalysisService = marketAnalysisService;
    }

    @Override
    @Transactional
    public void importIndicators(List<EconomicIndicator> indicators) {
        if (indicators == null || indicators.isEmpty()) {
            return;
        }

        for (EconomicIndicator indicator : indicators) {
            LocalDateTime utc = indicator.getUtcTime();
            if (utc != null) {
                indicator.setJstTime(utc.plusHours(9));
            }
        }

        List<VolatilityData> allVolatilityData = volatilityDataService.getAllVolatility();

        Map<EconomicIndicator, VolatilityData> linkedDataMap = 
            volatilityLinkingService.linkIndicatorsToVolatility(indicators, allVolatilityData);

        for (EconomicIndicator indicator : indicators) {
            VolatilityData linkedVolatility = linkedDataMap.get(indicator);
            if (linkedVolatility != null) {
                indicator.setLinkedVolatility(linkedVolatility);

                double currentVolatility = linkedVolatility.getVolatility();
                double avgVolatility5y = 0.0050;
                double stdDevVolatility5y = 0.0020;
                if (indicator.getName().contains("CPI")) {
                    avgVolatility5y = 0.0030;
                    stdDevVolatility5y = 0.0010;
                }
                
                MarketCondition marketCondition = marketAnalysisService.classifyMarketCondition(
                    currentVolatility, avgVolatility5y, stdDevVolatility5y
                );
                indicator.setMarketCondition(marketCondition.getDisplayName());

                IndicatorDirection direction = INDICATOR_DIRECTIONS.getOrDefault(indicator.getName(), IndicatorDirection.POSITIVE);
                MarketSentiment marketSentiment = marketAnalysisService.determineMarketSentiment(
                    indicator.getActual(), 
                    indicator.getForecast(), 
                    indicator.getPrevious(), 
                    direction
                );
                indicator.setSentiment(marketSentiment.getDisplayName());
            }
            economicIndicatorRepository.save(indicator);
        }
    }

    @Override
    public List<EconomicIndicator> getAllIndicators() {
        return economicIndicatorRepository.findAll();
    }

    @Override
    public List<EconomicIndicator> getRecentIndicators(String name, LocalDateTime jstTime) {
        return economicIndicatorRepository.findTop2ByNameAndJstTimeBeforeOrderByJstTimeDesc(name, jstTime);
    }

    @Override
    public Optional<EconomicIndicator> getIndicatorById(Long id) {
        return economicIndicatorRepository.findById(id);
    }

    @Override
    @Transactional
    public EconomicIndicator saveIndicator(EconomicIndicator indicator) {
        // JST変換 (新規作成時やutcTimeが更新された場合)
        if (indicator.getJstTime() == null && indicator.getUtcTime() != null) {
            indicator.setJstTime(indicator.getUtcTime().plusHours(9));
        }

        // ボラティリティ紐付けと分析 (新規作成時や関連データが更新された場合に実行するのが理想だが、ここでは毎回行う)
        // 紐付けのためにVolatilityDataを取得
        // 効率化のため、対象指標の日付に近いものだけを取得するなどの工夫が可能
        List<VolatilityData> allVolatilityData = volatilityDataService.getAllVolatility(); 
        Map<EconomicIndicator, VolatilityData> linkedDataMap = 
            volatilityLinkingService.linkIndicatorsToVolatility(List.of(indicator), allVolatilityData);
        
        VolatilityData linkedVolatility = linkedDataMap.get(indicator);
        if (linkedVolatility != null) {
            indicator.setLinkedVolatility(linkedVolatility);

            // --- ここから分析処理 ---
            double currentVolatility = linkedVolatility.getVolatility();
            // TODO: 本来はDB等から取得する5年平均と標準偏差。ここではダミー値を使用。
            double avgVolatility5y = 0.0050; 
            double stdDevVolatility5y = 0.0020; 
            if (indicator.getName().contains("CPI")) { 
                avgVolatility5y = 0.0030;
                stdDevVolatility5y = 0.0010;
            }
            
            MarketCondition marketCondition = marketAnalysisService.classifyMarketCondition(
                currentVolatility, avgVolatility5y, stdDevVolatility5y
            );
            indicator.setMarketCondition(marketCondition.getDisplayName());

            IndicatorDirection direction = INDICATOR_DIRECTIONS.getOrDefault(indicator.getName(), IndicatorDirection.POSITIVE); 
            MarketSentiment marketSentiment = marketAnalysisService.determineMarketSentiment(
                indicator.getActual(), 
                indicator.getForecast(), 
                indicator.getPrevious(), 
                direction
            );
            indicator.setSentiment(marketSentiment.getDisplayName());
            // --- 分析処理ここまで ---
        } else {
            // 紐づくボラティリティがない場合は関連分析結果をクリアすることも検討
            indicator.setLinkedVolatility(null);
            indicator.setMarketCondition(null);
            // indicator.setSentiment(null); // センチメントはボラティリティに依存しない場合もあるので注意
        }
        return economicIndicatorRepository.save(indicator);
    }

    @Override
    @Transactional
    public void deleteIndicator(Long id) {
        economicIndicatorRepository.deleteById(id);
    }
} 
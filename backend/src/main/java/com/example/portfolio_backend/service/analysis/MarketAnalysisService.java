package com.example.portfolio_backend.service.analysis;

public interface MarketAnalysisService {

    MarketCondition classifyMarketCondition(double currentVolatility, double avgVolatility5y, double stdDevVolatility5y);

    MarketSentiment determineMarketSentiment(Double actual, Double forecast, Double previous, IndicatorDirection direction);

    // 将来的に、EconomicIndicatorオブジェクト全体を受け取って分析するメソッドも考えられる
    // void analyzeEconomicIndicator(EconomicIndicator indicator);
} 
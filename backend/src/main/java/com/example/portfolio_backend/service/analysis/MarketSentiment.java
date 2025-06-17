package com.example.portfolio_backend.service.analysis;

public enum MarketSentiment {
    STRONG("強気"),
    NEUTRAL("普通"),
    WEAK("弱気");

    private final String displayName;

    MarketSentiment(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 
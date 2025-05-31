package com.example.portfolio_backend.service.analysis;

public enum MarketCondition {
    LARGE("大"),
    MEDIUM("中"),
    SMALL("小");

    private final String displayName;

    MarketCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 
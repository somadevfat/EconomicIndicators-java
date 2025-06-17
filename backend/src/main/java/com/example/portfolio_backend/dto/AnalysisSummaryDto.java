package com.example.portfolio_backend.dto;

import java.util.List;

import com.example.portfolio_backend.entity.EconomicIndicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSummaryDto {
    private EconomicIndicator currentIndicator; // 現在の指標データ
    private List<EconomicIndicator> recentIndicators; // 直近2回の指標データ（紐付けボラティリティ含む）
    // marketCondition と sentiment は currentIndicator 内に含まれるため、DTOのフィールドとしては不要かも。
    // ただし、APIのレスポンスとして明示的に分けたい場合は追加する。
    // private String marketCondition;
    // private String sentiment;
} 
package com.example.portfolio_backend.service.analysis;

import java.util.List;
import java.util.Map;

import com.example.portfolio_backend.entity.VolatilityData;

public interface AverageVolatilityService {
    /**
     * 指定されたボラティリティデータ一覧から、通貨ペア|時間帯ごとの平均ボラティリティを計算して返します。
     * @param dataList ボラティリティデータ一覧
     * @return key: pair|timeSlot, value: 平均ボラティリティ
     */
    Map<String, Double> calculateAverageVolatility(List<VolatilityData> dataList);
} 
package com.example.portfolio_backend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.entity.VolatilityData;

@Service
public class VolatilityLinkingService {
    /**
     * 指定された経済指標リストとボラティリティデータリストを時間帯ベースで紐付ける。
     *
     * @param indicators 経済指標リスト
     * @param data ボラティリティデータリスト
     * @return 経済指標ごとの最も近いボラティリティデータのマッピング
     */
    public Map<EconomicIndicator, VolatilityData> linkByTimeSlot(List<EconomicIndicator> indicators, List<VolatilityData> data) {
        // サーバータイムゾーン（キプロス標準時／夏時間）
        ZoneId serverZone = ZoneId.of("Europe/Nicosia");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        // 日付ごとのデータグルーピング
        var dataByDate = data.stream()
            .collect(Collectors.groupingBy(VolatilityData::getDate));
        Map<EconomicIndicator, VolatilityData> result = new HashMap<>();
        for (EconomicIndicator indicator : indicators) {
            // JST → サーバー時間に変換
            ZonedDateTime indServerZdt = indicator.getJstTime()
                .atZone(ZoneId.of("Asia/Tokyo"))
                .withZoneSameInstant(serverZone);
            LocalDate indDate = indServerZdt.toLocalDate();
            var dailyData = dataByDate.getOrDefault(indDate, Collections.emptyList());
            if (dailyData.isEmpty()) continue;
            // 時刻ソート済みリスト作成
            var sorted = dailyData.stream()
                .map(d -> new AbstractMap.SimpleEntry<>(
                    LocalTime.parse(d.getTimeSlot(), timeFormatter), d))
                .sorted(Entry.comparingByKey())
                .collect(Collectors.toList());
            var times = sorted.stream()
                .map(Entry::getKey)
                .collect(Collectors.toList());
            LocalTime targetTime = indServerZdt.toLocalTime();
            // 二分探索でインデックス取得
            int idx = Collections.binarySearch(times, targetTime);
            if (idx < 0) idx = -idx - 1;
            int nearestIdx;
            if (idx == 0) {
                nearestIdx = 0;
            } else if (idx >= times.size()) {
                nearestIdx = times.size() - 1;
            } else {
                long diffPrev = Duration.between(times.get(idx - 1), targetTime).abs().toMinutes();
                long diffNext = Duration.between(times.get(idx), targetTime).abs().toMinutes();
                nearestIdx = diffPrev <= diffNext ? idx - 1 : idx;
            }
            VolatilityData nearestData = sorted.get(nearestIdx).getValue();
            result.put(indicator, nearestData);
        }
        return result;
    }
} 
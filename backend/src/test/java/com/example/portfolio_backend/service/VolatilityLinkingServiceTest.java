package com.example.portfolio_backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.entity.VolatilityData;

public class VolatilityLinkingServiceTest {

    private VolatilityLinkingService service;

    @BeforeEach
    void setUp() {
        service = new VolatilityLinkingService();
    }

    @Test
    void testExactMatch() {
        // JST 10:00 -> server time (キプロス) 03:00 (冬時間 UTC+2)
        LocalDateTime jstTime = LocalDateTime.of(2025, 5, 31, 10, 0);
        EconomicIndicator indicator = new EconomicIndicator(
            null, null, jstTime,
            "test", "JP", null, null, null,
            null, null, null, null, null
        );
        // VolatilityData at date 2025-05-31 with timeSlot 03:00
        VolatilityData data = new VolatilityData(
            null,
            LocalDate.of(2025, 5, 31),
            "03:00", "EURUSD",
            null, null, null,
            null, null
        );
        Map<EconomicIndicator, VolatilityData> result = service.linkByTimeSlot(
            List.of(indicator), List.of(data)
        );
        assertThat(result).containsEntry(indicator, data);
    }

    @Test
    void testNearestMatch() {
        // JST 10:15 -> server time 04:15 (夏時間 UTC+3)
        LocalDateTime jstTime = LocalDateTime.of(2025, 5, 31, 10, 15);
        EconomicIndicator indicator = new EconomicIndicator(
            null, null, jstTime,
            "test2", "JP", null, null, null,
            null, null, null, null, null
        );
        VolatilityData before = new VolatilityData(
            null, LocalDate.of(2025, 5, 31), "03:00", "EURUSD",
            null, null, null, null, null
        );
        VolatilityData after = new VolatilityData(
            null, LocalDate.of(2025, 5, 31), "03:30", "EURUSD",
            null, null, null, null, null
        );
        // 03:00 と 03:30 の間、JST10:15はserver time 04:15に対応 -> 最も近い03:30を選択
        Map<EconomicIndicator, VolatilityData> result = service.linkByTimeSlot(
            List.of(indicator), List.of(before, after)
        );
        assertThat(result.get(indicator)).isEqualTo(after);
    }

    @Test
    void testNoDataForDate() {
        LocalDateTime jstTime = LocalDateTime.of(2025, 5, 30, 12, 0);
        EconomicIndicator indicator = new EconomicIndicator(
            null, null, jstTime,
            "test3", "JP", null, null, null,
            null, null, null, null, null
        );
        // data list empty
        Map<EconomicIndicator, VolatilityData> result = service.linkByTimeSlot(
            List.of(indicator), List.of()
        );
        assertThat(result).doesNotContainKey(indicator);
    }
} 
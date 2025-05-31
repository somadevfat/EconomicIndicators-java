package com.example.portfolio_backend.service.analysis;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.example.portfolio_backend.entity.VolatilityData;

class AverageVolatilityServiceTest {

    private final AverageVolatilityService service = new AverageVolatilityServiceImpl();

    @Test
    void testCalculateAverageVolatility() {
        VolatilityData d1 = new VolatilityData();
        d1.setDate(LocalDate.of(2020, 1, 1));
        d1.setTimeSlot("07:00");
        d1.setPair("EURUSD");
        d1.setVolatility(0.1);

        VolatilityData d2 = new VolatilityData();
        d2.setDate(LocalDate.of(2020, 1, 2));
        d2.setTimeSlot("07:00");
        d2.setPair("EURUSD");
        d2.setVolatility(0.3);

        VolatilityData d3 = new VolatilityData();
        d3.setDate(LocalDate.of(2020, 1, 3));
        d3.setTimeSlot("08:00");
        d3.setPair("EURUSD");
        d3.setVolatility(0.2);

        List<VolatilityData> list = List.of(d1, d2, d3);

        Map<String, Double> result = service.calculateAverageVolatility(list);

        String key1 = "EURUSD|07:00";
        String key2 = "EURUSD|08:00";

        assertEquals(0.2, result.get(key1), 1e-6);
        assertEquals(0.2, result.get(key2), 1e-6);
        assertEquals(2, result.size());
    }
} 
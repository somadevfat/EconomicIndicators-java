package com.example.portfolio_backend.service.analysis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.portfolio_backend.entity.VolatilityData;

@Service
public class AverageVolatilityServiceImpl implements AverageVolatilityService {

    @Override
    public Map<String, Double> calculateAverageVolatility(List<VolatilityData> dataList) {
        return dataList.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getPair() + "|" + d.getTimeSlot(),
                        Collectors.averagingDouble(VolatilityData::getVolatility)
                ));
    }
} 
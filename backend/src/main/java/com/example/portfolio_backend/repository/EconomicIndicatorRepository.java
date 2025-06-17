package com.example.portfolio_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.portfolio_backend.entity.EconomicIndicator;
 
@Repository
public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {
    List<EconomicIndicator> findTop2ByNameAndJstTimeBeforeOrderByJstTimeDesc(String name, LocalDateTime jstTime);

    // 必要に応じて、現在の指標IDを除外するバージョンも検討 (同一指標の更新時など)
    // List<EconomicIndicator> findTop2ByIdNotAndNameAndJstTimeBeforeOrderByJstTimeDesc(Long id, String name, LocalDateTime jstTime);
} 
package com.example.portfolio_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.portfolio_backend.entity.EconomicIndicator;
import com.example.portfolio_backend.service.EconomicIndicatorService;

@RestController
@RequestMapping("/api/indicators")
public class EconomicIndicatorController {

    private final EconomicIndicatorService economicIndicatorService;

    public EconomicIndicatorController(EconomicIndicatorService economicIndicatorService) {
        this.economicIndicatorService = economicIndicatorService;
    }

    // POST /api/indicators: 経済指標作成 (単体)
    @PostMapping
    public ResponseEntity<EconomicIndicator> createIndicator(@RequestBody EconomicIndicator indicator) {
        // IDはnullであることを期待 (新規作成のため)
        indicator.setId(null);
        EconomicIndicator savedIndicator = economicIndicatorService.saveIndicator(indicator);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedIndicator);
    }

    // GET /api/indicators: 経済指標一覧取得
    @GetMapping
    public ResponseEntity<List<EconomicIndicator>> getAllIndicators() {
        List<EconomicIndicator> indicators = economicIndicatorService.getAllIndicators();
        return ResponseEntity.ok(indicators);
    }

    // GET /api/indicators/{id}: 経済指標詳細取得
    @GetMapping("/{id}")
    public ResponseEntity<EconomicIndicator> getIndicatorById(@PathVariable Long id) {
        Optional<EconomicIndicator> indicator = economicIndicatorService.getIndicatorById(id);
        return indicator.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /api/indicators/{id}: 経済指標更新
    @PutMapping("/{id}")
    public ResponseEntity<EconomicIndicator> updateIndicator(@PathVariable Long id, @RequestBody EconomicIndicator indicatorDetails) {
        Optional<EconomicIndicator> existingIndicatorOptional = economicIndicatorService.getIndicatorById(id);
        if (existingIndicatorOptional.isPresent()) {
            EconomicIndicator existingIndicator = existingIndicatorOptional.get();
            // indicatorDetails の内容を existingIndicator にコピー (IDは変更しない)
            // 本来はBeanUtils.copyProperties等を使うか、必要なフィールドのみを選択的に更新する
            existingIndicator.setUtcTime(indicatorDetails.getUtcTime());
            existingIndicator.setJstTime(null); // saveIndicator内で再計算させる
            existingIndicator.setName(indicatorDetails.getName());
            existingIndicator.setCountry(indicatorDetails.getCountry());
            existingIndicator.setActual(indicatorDetails.getActual());
            existingIndicator.setForecast(indicatorDetails.getForecast());
            existingIndicator.setPrevious(indicatorDetails.getPrevious());
            // linkedVolatility, marketCondition, sentiment は saveIndicator 内で再計算される想定
            
            EconomicIndicator updatedIndicator = economicIndicatorService.saveIndicator(existingIndicator);
            return ResponseEntity.ok(updatedIndicator);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/indicators/{id}: 経済指標削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndicator(@PathVariable Long id) {
        if (economicIndicatorService.getIndicatorById(id).isPresent()) {
            economicIndicatorService.deleteIndicator(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/indicators/import: 経済指標JSONインポート
    @PostMapping("/import")
    public ResponseEntity<Void> importIndicators(@RequestBody List<EconomicIndicator> indicators) {
        economicIndicatorService.importIndicators(indicators);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
} 
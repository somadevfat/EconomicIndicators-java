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

import com.example.portfolio_backend.entity.VolatilityData;
import com.example.portfolio_backend.service.VolatilityDataService;

@RestController
@RequestMapping("/api/volatility")
public class VolatilityDataController {

    private final VolatilityDataService volatilityDataService;

    public VolatilityDataController(VolatilityDataService volatilityDataService) {
        this.volatilityDataService = volatilityDataService;
    }

    // POST /api/volatility: ボラティリティデータ作成 (単体)
    @PostMapping
    public ResponseEntity<VolatilityData> createVolatilityData(@RequestBody VolatilityData volatilityData) {
        volatilityData.setId(null); // 新規作成のためIDはnull
        VolatilityData savedData = volatilityDataService.saveVolatilityData(volatilityData);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
    }

    // GET /api/volatility: ボラティリティデータ一覧取得
    @GetMapping
    public ResponseEntity<List<VolatilityData>> getAllVolatilityData() {
        List<VolatilityData> volatilityDataList = volatilityDataService.getAllVolatility();
        return ResponseEntity.ok(volatilityDataList);
    }

    // GET /api/volatility/{id}: ボラティリティデータ詳細取得
    @GetMapping("/{id}")
    public ResponseEntity<VolatilityData> getVolatilityDataById(@PathVariable Long id) {
        Optional<VolatilityData> volatilityData = volatilityDataService.getVolatilityDataById(id);
        return volatilityData.map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PUT /api/volatility/{id}: ボラティリティデータ更新
    @PutMapping("/{id}")
    public ResponseEntity<VolatilityData> updateVolatilityData(@PathVariable Long id, @RequestBody VolatilityData volatilityDataDetails) {
        Optional<VolatilityData> existingDataOptional = volatilityDataService.getVolatilityDataById(id);
        if (existingDataOptional.isPresent()) {
            VolatilityData existingData = existingDataOptional.get();
            // volatilityDataDetails の内容を existingData にコピー (IDは変更しない)
            existingData.setDate(volatilityDataDetails.getDate());
            existingData.setTimeSlot(volatilityDataDetails.getTimeSlot());
            existingData.setPair(volatilityDataDetails.getPair());
            existingData.setHigh(volatilityDataDetails.getHigh());
            existingData.setLow(volatilityDataDetails.getLow());
            existingData.setVolatility(volatilityDataDetails.getVolatility());
            
            VolatilityData updatedData = volatilityDataService.saveVolatilityData(existingData);
            return ResponseEntity.ok(updatedData);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/volatility/{id}: ボラティリティデータ削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVolatilityData(@PathVariable Long id) {
        if (volatilityDataService.getVolatilityDataById(id).isPresent()) {
            volatilityDataService.deleteVolatilityData(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/volatility/import: ZigZagボラティリティJSONインポート
    @PostMapping("/import")
    public ResponseEntity<Void> importVolatility(@RequestBody List<VolatilityData> volatilityDataList) {
        volatilityDataService.importVolatility(volatilityDataList);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
} 
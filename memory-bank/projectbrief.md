# 経済指標・ボラティリティ分析システム 要件定義書 v3.0

## 1. プロジェクト概要

### 1.1 プロジェクト名
経済指標・ボラティリティ分析システム（バックエンド API + フロントエンド管理画面）

### 1.2 プロジェクト期間
- 開始日：2025年5月30日（金）
- 終了予定日：2025年5月31日（土）
- 開発期間：2日間（フル実装）

### 1.3 プロジェクト目的
MT5から出力された経済指標とZigZagボラティリティデータを分析し、地合い判定と統計分析を行うシステムを構築。バックエンドエンジニアとしてのスキルセットを実証する。
- 手動分析を自動化：従来手作業で行っていた経済指標とボラティリティの相関分析を完全自動化
- トレードしても大丈夫かをひと目でわかるように：リスク評価と市場状況を視覚的に分かりやすく表示

### 1.4 システム構成
- **バックエンド**：Spring Boot REST API（Docker化）
- **フロントエンド**：React + TypeScript SPA
- **データベース**：MySQL（Docker）
- **データソース**：MT5（経済指標 + ZigZagボラティリティ）
- **開発環境**：VSCode + Docker

### 1.5 表示対象データ（最終目標）
システムが最終的に表示する3つの項目：
1. **5年分データ平均からの地合い分類**（大中小の3分類）
2. **今日の指標の直近2回の実績値**
3. **地合い判定**（直近2回が平均値からどれくらいずれているかで判定）

## 2. データソース仕様

### 2.1 MT5データ出力仕様

#### 2.1.1 経済指標JSON（カレンダー情報）
```json
{
  "indicators": [
    {
      "time": "2025-05-30T21:30:00Z",  // UTC時刻
      "name": "Non-Farm Payrolls",
      "country": "US",
      "actual": 210000,
      "forecast": 200000,
      "previous": 195000
    },
    {
      "time": "2025-05-30T12:30:00Z",
      "name": "CPI",
      "country": "US", 
      "actual": 3.2,
      "forecast": 3.1,
      "previous": 3.0
    }
  ]
}
```

#### 2.1.2 ZigZagボラティリティJSON（チャート分析結果）
```json
{
  "volatilityData": [
    {
      "date": "2025-05-30",
      "timeSlot": "07-09",  // UTC時間帯
      "pair": "EURUSD",
      "high": 1.0850,
      "low": 1.0820,
      "volatility": 0.0030
    },
    {
      "date": "2025-05-30", 
      "timeSlot": "09-12",
      "pair": "EURUSD",
      "high": 1.0865,
      "low": 1.0835,
      "volatility": 0.0030
    },
    {
      "date": "2025-05-30",
      "timeSlot": "12-16", 
      "pair": "EURUSD",
      "high": 1.0875,
      "low": 1.0840,
      "volatility": 0.0035
    }
  ]
}
```

#### 2.1.3 時間帯区分（UTC → JST変換後に使用）
- **07-09時**（JST 16-18時）
- **09-12時**（JST 18-21時）
- **12-16時**（JST 21-01時）
- **16-21時**（JST 01-06時）
- **21-24時**（JST 06-09時）
- **00-03時**（JST 09-12時）
- **03-07時**（JST 12-16時）

### 2.2 データ出力頻度・タイミング
- **頻度**：毎日1回（早朝実行）
- **5年分履歴データ**：事前にローカルPC（MT5）で出力し、初期インポート
- **日次データ**：VPS（MT5）から毎日自動出力

## 3. システム要件

### 3.1 バックエンド機能要件

#### 3.1.1 データインポート機能
- **経済指標JSONインポート**
- **ZigZagボラティリティJSONインポート**
- **UTC時刻→JST時刻変換**
- **時間帯ベースでの経済指標-ボラティリティ紐付け**
- **5年分履歴データ一括インポート**

#### 3.1.2 時間帯紐付けロジック
```java
// 経済指標発表時刻（JST）→ 該当時間帯のボラティリティを検索・紐付け
public class VolatilityLinkingService {
    public void linkIndicatorToVolatility(EconomicIndicator indicator, List<VolatilityData> volatilities) {
        // 1. UTC→JST変換
        LocalDateTime jstTime = convertUtcToJst(indicator.getTime());
        
        // 2. JST時刻から時間帯判定
        TimeSlot timeSlot = determineTimeSlot(jstTime);
        
        // 3. 該当時間帯のボラティリティデータを検索
        VolatilityData matched = findVolatilityByTimeSlot(volatilities, timeSlot, jstTime.toLocalDate());
        
        // 4. 紐付け保存
        indicator.setLinkedVolatility(matched);
    }
}
```

#### 3.1.3 統計分析機能
- **5年分平均ボラティリティ計算**
- **地合い3分類アルゴリズム**（大中小）
- **直近2回データ取得**（同一指標名）
- **地合い判定ロジック**（直近2回の平均からのずれ具合）

#### 3.1.4 REST API
- **経済指標CRUD操作**
- **ボラティリティデータ管理**
- **統計分析結果取得**
- **JSONインポート処理**

#### 3.1.5 セキュリティ・品質管理
- **Spring Security + JWT認証**
- **Bean Validation**
- **GlobalExceptionHandler**
- **OpenAPI/Swagger統合**

### 3.2 フロントエンド機能要件

#### 3.2.1 メイン表示画面
指標ごとに以下3項目を表示：
1. **地合い分類**：「大」「中」「小」
2. **直近2回実績**：過去2回の発表値とボラティリティ
3. **地合い判定**：「強気」「普通」「弱気」等

#### 3.2.2 管理機能
- **データインポート画面**（JSON一括アップロード）
- **経済指標一覧・詳細表示**
- **統計分析結果表示**

## 4. データモデル設計

### 4.1 エンティティ設計

```java
// 経済指標エンティティ
@Entity
public class EconomicIndicator {
    @Id @GeneratedValue
    private Long id;
    
    private LocalDateTime utcTime;          // UTC時刻（元データ）
    private LocalDateTime jstTime;          // JST変換済み時刻
    private String name;                    // 指標名
    private String country;                 // 国
    private Double actual;                  // 実際値
    private Double forecast;                // 予測値
    private Double previous;                // 前回値
    
    @OneToOne
    private VolatilityData linkedVolatility; // 紐付けボラティリティ
    
    // 統計分析結果
    private String marketCondition;         // 地合い分類（大中小）
    private String sentiment;               // 地合い判定
}

// ボラティリティデータ
@Entity
public class VolatilityData {
    @Id @GeneratedValue
    private Long id;
    
    private LocalDate date;
    // ... existing code ...
    // This is a new file, so no existing code.
    // ... existing code ...
}
``` 
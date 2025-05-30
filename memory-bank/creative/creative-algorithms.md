🎨🎨🎨 ENTERING CREATIVE PHASE: ALGORITHM 🎨🎨🎨

Focus: Algorithm Design for Market Condition and Sentiment Analysis
Objective: Define clear and implementable logic for classifying market condition (大, 中, 小) and determining market sentiment (強気, 普通, 弱気).
Requirements: Refer to `projectbrief.md` (sections 1.5, 3.1.3) and `tasks.md` (section 1.7).

## 1. 地合い3分類アルゴリズム (市場状況分類)

### 1.1 コンポーネント記述
特定の経済指標発表時の紐付けられたボラティリティを、過去の平均的なボラティリティと比較し、「大」「中」「小」の3段階で市場状況（地合い）を分類するアルゴリズム。

### 1.2 要件と制約
- 入力: 
    - 対象指標発表時のボラティリティ実績値 (`current_volatility`)
    - 過去5年間の同指標・同時間帯のボラティリティデータ群 (またはその統計値、特に平均値 `avg_vol_5y` と標準偏差 `stddev_vol_5y`)
- 出力: 「大」「中」「小」のいずれかの文字列。
- 計算基礎: `projectbrief.md` 1.5 にある「5年分データ平均からの地合い分類」に基づく。
- 客観的かつある程度再現性のある分類であること。

### 1.3 オプション分析

基準となるのは「過去5年間の平均ボラティリティ (`avg_vol_5y`)」。問題は、`current_volatility` がこの平均からどれだけ乖離したら「大」「中」「小」と判断するか。

#### オプション1: 固定倍率閾値
- **記述**: 平均値に対する固定の倍率で閾値を設定。
    - 例: 
        - `current_volatility >= avg_vol_5y * 2.0` なら「大」
        - `avg_vol_5y * 0.5 <= current_volatility < avg_vol_5y * 2.0` なら「中」
        - `current_volatility < avg_vol_5y * 0.5` なら「小」
- **Pros**:
    - ロジックが非常にシンプルで理解しやすい。
    - 実装が容易。
- **Cons**:
    - 全ての指標や通貨ペア、時間帯で同じ倍率が適切とは限らない。ボラティリティの絶対値が小さい指標では少しの変動で「大」になりやすく、逆に大きい指標では「大」になりにくい可能性がある。
    - ボラティリティの分布の形状（正規分布に近いか、裾が重いかなど）を考慮していない。

#### オプション2: 標準偏差 (σ) を利用した閾値
- **記述**: 過去5年データの平均値 (`avg_vol_5y`) と標準偏差 (`stddev_vol_5y`) を利用。
    - 例:
        - `current_volatility >= avg_vol_5y + 1.5 * stddev_vol_5y` なら「大」
        - `avg_vol_5y - 1.0 * stddev_vol_5y <= current_volatility < avg_vol_5y + 1.5 * stddev_vol_5y` なら「中」
        - `current_volatility < avg_vol_5y - 1.0 * stddev_vol_5y` なら「小」 (ただしボラティリティは通常非負なので下限は0や`avg_vol_5y * 0.1`等も考慮)
- **Pros**:
    - データのばらつき（標準偏差）を考慮するため、指標ごとの特性に対応しやすい。
    - 統計的な根拠に基づいた分類と言える。
    - 「平均からどれくらい離れているか」を相対的に評価できる。
- **Cons**:
    - 標準偏差の計算が追加で必要（バッチ処理で事前に計算しておくことが望ましい）。
    - 閾値の係数（例: 1.5σ, 1.0σ）を適切に設定するためのドメイン知識や調整が必要になる場合がある。
    - ボラティリティデータが正規分布に従わない場合、標準偏差の解釈が難しくなることがある（ただし、実用上は有効な場合が多い）。

#### オプション3: パーセンタイルを利用した閾値
- **記述**: 過去5年データのボラティリティの分布におけるパーセンタイル点を利用。
    - 例:
        - `current_volatility` が過去データの80パーセンタイル値以上なら「大」
        - 過去データの20パーセンタイル値以上、80パーセンタイル値未満なら「中」
        - 過去データの20パーセンタイル値未満なら「小」
- **Pros**:
    - データの分布形状に依存せず、頑健な分類が可能（外れ値の影響を受けにくい）。
    - 「上位20%の活発さ」「下位20%の静けさ」といった直感的な解釈が可能。
- **Cons**:
    - パーセンタイル点の計算が必要（バッチ処理で事前に計算しておくことが望ましい）。
    - どのパーセンタイル点（例: 80/20, 75/25）を閾値とするかの決定が必要。

### 1.4 推奨アプローチ: オプション2 (標準偏差 (σ) を利用した閾値)
- **理由**: 固定倍率よりもデータの特性を捉えられ、パーセンタイルよりも閾値の「意味（平均からどの程度離れているか）」が解釈しやすい。標準偏差は多くの統計的アプローチで用いられる基本的な指標であり、閾値の係数を調整することで感度をコントロールできる。過去5年分のデータがあれば、平均と標準偏差は安定した値として事前に計算・保存しておける。
    - ボラティリティが負になることはないので、「小」の判定で `avg_vol_5y - X * stddev_vol_5y` が負になる場合は、0や非常に小さい正の値を下限とする（例：`max(0, avg_vol_5y - 1.0 * stddev_vol_5y)`）。実務的には、ボラティリティが極端に低い場合も「小」と見なせるため、`avg_vol_5y * 0.2` のような値を下限にするなども考えられる。

### 1.5 実装ガイドライン

1.  **事前計算**: 各経済指標名 (`name`)、国 (`country`)、通貨ペア (`pair`、ボラティリティデータに依存)、時間帯 (`time_slot`) の組み合わせごとに、過去5年間のボラティリティデータの平均値 (`avg_vol_5y`) と標準偏差 (`stddev_vol_5y`) を計算し、`average_volatilities` テーブル（アーキテクチャ設計で提案済み）または類似のテーブルに保存しておく。
    - 通貨ペアや時間帯をキーに含めるか否かは、どの粒度で平均と比較するかに依存する。要件定義からは指標名と時間帯が重要そうである。
    - `projectbrief.md` 2.1.3の時間帯区分を元に、指標発表時刻（JST）がどの時間帯に属するかを判定し、その時間帯のボラティリティ平均と比較する。

2.  **分類ロジックの仮実装 (Java例)**:
    ```java
    public enum MarketCondition {
        LARGE("大"),
        MEDIUM("中"),
        SMALL("小");
        // ... constructor, getter
    }

    public MarketCondition classifyMarketCondition(double currentVolatility, double avgVolatility5y, double stdDevVolatility5y) {
        if (stdDevVolatility5y <= 0) { // 標準偏差が0または負（データ不足など）の場合は固定倍率にフォールバック
            if (currentVolatility >= avgVolatility5y * 1.8) return MarketCondition.LARGE;
            if (currentVolatility < avgVolatility5y * 0.6) return MarketCondition.SMALL;
            return MarketCondition.MEDIUM;
        }

        // 閾値の係数 (これらの値は調整可能)
        double largeThresholdMultiplier = 1.5;
        double smallThresholdMultiplier = 1.0; // 平均より下に1σ

        double upperLimitForMedium = avgVolatility5y + largeThresholdMultiplier * stdDevVolatility5y;
        double lowerLimitForMedium = avgVolatility5y - smallThresholdMultiplier * stdDevVolatility5y;
        
        // ボラティリティは0以上なので、下限が0未満にならないように調整
        lowerLimitForMedium = Math.max(0.0, lowerLimitForMedium);
        // さらに、平均値の極端に低い値もSMALLと見なす場合 (例: 平均の20%未満)
        // lowerLimitForMedium = Math.max(avgVolatility5y * 0.2, lowerLimitForMedium);

        if (currentVolatility >= upperLimitForMedium) {
            return MarketCondition.LARGE;
        } else if (currentVolatility < lowerLimitForMedium) { //先にSMALLを判定
            return MarketCondition.SMALL;
        }
        // SMALLにもLARGEにも当てはまらない場合
        return MarketCondition.MEDIUM; 
    }
    ```

3.  **閾値係数の調整**: `largeThresholdMultiplier` (例: 1.5) や `smallThresholdMultiplier` (例: 1.0) は、実際のデータ分布やビジネス要件（どの程度の変動を「大」と見なすか）に応じて調整が必要。初期値として設定し、運用しながら見直す。

4.  **エッジケース**: 
    - 過去データが不足していて標準偏差が計算できない、または非常に小さい場合：固定倍率（オプション1）へのフォールバックロジックを設ける。
    - ボラティリティが0の場合：通常「小」と分類される。

### 1.6 検証
- [X] 入出力が明確である。
- [X] 統計的アプローチ（標準偏差）に基づいている。
- [X] 閾値の調整可能性が考慮されている。
- [X] エッジケース（データ不足時）の対応が考慮されている。

## 2. 地合い判定アルゴリズム (市場センチメント判定)

### 2.1 コンポーネント記述
経済指標の「実績値 (Actual)」「市場予想 (Forecast)」「前回値 (Previous)」の3つの数値を比較し、市場のセンチメントを「強気」「普通」「弱気」のいずれかに分類するアルゴリズム。

### 2.2 要件と制約
- 入力:
    - 実績値 (`actual`)
    - 市場予想値 (`forecast`)
    - 前回値 (`previous`)
- 出力: 「強気」「普通」「弱気」のいずれかの文字列。
- 計算基礎: `projectbrief.md` 1.5 にある「実績・市場予想・前回値の比較による地合い判定」に基づく。
- 指標の特性（例：高いほど良いか、低いほど良いか）を考慮できること。

### 2.3 オプション分析

この判定は、指標が「ポジティブサプライズ（予想より良い）」「ネガティブサプライズ（予想より悪い）」か、そして「前回よりも改善しているか、悪化しているか」の組み合わせで決まることが多い。

多くの経済指標は「数値が高いほど良い」（例：GDP成長率、製造業PMI）ですが、「数値が低いほど良い」（例：失業率）指標も存在します。この特性を「指標の方向性 (direction)」として考慮する必要があります (`direction = POSITIVE` なら高いほど良い、`direction = NEGATIVE` なら低いほど良い)。

#### オプション1: ルールベース・優先度付き評価
- **記述**: 実績vs予想、実績vs前回の比較結果に基づき、複数のルールを定義し、優先度に従って評価する。
    - 例 (direction = POSITIVE の場合):
        1. 実績 > 予想 AND 実績 > 前回  => 「強気」
        2. 実績 > 予想 AND 実績 <= 前回 => 「強気」(予想を上回ったことを重視) または 「普通」(前回よりは悪化)
        3. 実績 <= 予想 AND 実績 > 前回 => 「普通」(予想を下回ったが前回よりは改善)
        4. 実績 <= 予想 AND 実績 <= 前回 => 「弱気」
        5. 予想がない場合: 実績 > 前回 => 「強気」、実績 <= 前回 => 「弱気」
- **Pros**:
    - 人間の判断ロジックに近い形でルールを記述できる。
    - 特定のパターンに対する重み付け（例：予想を上回ることの重要性）をルールで表現しやすい。
- **Cons**:
    - ルールの数が多くなると複雑になり、網羅性や一貫性の担保が難しくなることがある。
    - 「普通」の範囲が広くなりがち、または細かく定義しすぎるとルールが増える。

#### オプション2: スコアリング方式
- **記述**: 各比較（実績vs予想、実績vs前回）に対してスコアを付与し、合計スコアでセンチメントを判定。
    - 例 (direction = POSITIVE の場合):
        - `score_vs_forecast`: 実績 > 予想 なら +2, 実績 == 予想 なら 0, 実績 < 予想 なら -2
        - `score_vs_previous`: 実績 > 前回 なら +1, 実績 == 前回 なら 0, 実績 < 前回 なら -1
        - `total_score = score_vs_forecast + score_vs_previous`
        - 判定: `total_score >= 3` => 「強気」、`1 <= total_score <= 2` => 「普通」、`total_score <= 0` => 「弱気」 (閾値は調整可能)
    - 予想がない場合は `score_vs_forecast` を0とするか、`score_vs_previous` の重みを増やす。
- **Pros**:
    - ルールベースより判定ロジックが単純化される場合がある。
    - スコアの重み付けや閾値を調整することで、センチメントの感度を調整しやすい。
    - 指標の方向性 (POSITIVE/NEGATIVE) は、スコア計算の符号を反転させることで容易に対応可能。
- **Cons**:
    - スコアの割り当て方や合計スコアの閾値設定に試行錯誤が必要な場合がある。
    - 特定の重要なパターン（例：予想を大幅に上回ったが、前回からは微減）のニュアンスがスコア合計だけでは表現しきれない可能性がある。

#### オプション3: 複合ルール + 一部スコアリング
- **記述**: 基本的な強気/弱気のパターンは明確なルールで定義し、それ以外の中間的なケースをスコアや副次的ルールで「普通」またはそれに近いセンチメントに分類する。
- **Pros**:
    - オプション1と2の利点を組み合わせられる可能性がある。
    - 明確なケースは直感的なルールで、曖昧なケースはスコアで柔軟に扱える。
- **Cons**:
    - 設計が複雑になる可能性がある。

### 2.4 推奨アプローチ: オプション2 (スコアリング方式)
- **理由**: 柔軟性と拡張性、そして指標の方向性（高い方が良いか、低い方が良いか）への対応のしやすさからスコアリング方式を推奨。スコアの付け方や閾値を調整することで、様々な指標のニュアンスに対応できる可能性がある。また、ルールの数が爆発的に増えることを防ぎやすい。
    - 特に「予想」データがない場合の扱いや、実績・予想・前回が同値の場合の扱いもスコアリングの中で定義しやすい。

### 2.5 実装ガイドライン

1.  **指標の方向性定義**: 各経済指標に対して、「高い方が良い (POSITIVE)」か「低い方が良い (NEGATIVE)」かを示す属性（`IndicatorDirection` enum: `POSITIVE`, `NEGATIVE`）をメタデータとして持つ。これは指標マスタなどで管理。

2.  **スコア計算ロジック (Java例)**:
    ```java
    public enum MarketSentiment {
        STRONG("強気"),
        NEUTRAL("普通"),
        WEAK("弱気");
        // ... constructor, getter
    }

    public enum IndicatorDirection {
        POSITIVE, // 高いほど良い (例: GDP)
        NEGATIVE  // 低いほど良い (例: 失業率)
    }

    public MarketSentiment determineMarketSentiment(Double actual, Double forecast, Double previous, IndicatorDirection direction) {
        // nullチェック: actualは必須。forecast, previousはnull許容
        if (actual == null) {
            return MarketSentiment.NEUTRAL; // またはエラー
        }

        int score = 0;

        // 実績 vs 予想 (forecast があれば)
        if (forecast != null) {
            if (actual > forecast) {
                score += (direction == IndicatorDirection.POSITIVE) ? 2 : -2;
            } else if (actual < forecast) {
                score += (direction == IndicatorDirection.POSITIVE) ? -2 : 2;
            }
            // actual == forecast ならスコア変動なし (0)
        }

        // 実績 vs 前回 (previous があれば)
        if (previous != null) {
            if (actual > previous) {
                score += (direction == IndicatorDirection.POSITIVE) ? 1 : -1;
            } else if (actual < previous) {
                score += (direction == IndicatorDirection.POSITIVE) ? -1 : 1;
            }
            // actual == previous ならスコア変動なし (0)
        }
        
        // 予想も前回もない場合: 評価不能としてNEUTRAL
        if (forecast == null && previous == null) {
             return MarketSentiment.NEUTRAL;
        }

        // スコアに基づいてセンチメントを決定 (閾値は調整可能)
        // 例: 予想がある場合は予想を重視 (スコア±2)、前回比較は補足的 (スコア±1)
        // 合計スコア: Strong(3), Neutral(1,2), Weak(-1,0以下) / Strong(2以上)、Neutral(0,1)、Weak(-1以下)
        if (score >= 2) { // 予想を上回り(下回り)、かつ前回以上(以下)など、明確にポジティブ
            return MarketSentiment.STRONG;
        } else if (score <= -1) { // 予想を下回り(上回り)、かつ前回以下(以上)など、明確にネガティブ
            return MarketSentiment.WEAK;
        } else { // score が 0 or 1 (POSITIVE指標で 予想通りだが前回より良い場合など)
            return MarketSentiment.NEUTRAL;
        }
    }
    ```

3.  **スコアと閾値の調整**: 上記のスコア配分（予想比較±2、前回比較±1）や、最終的なセンチメント判定の閾値（例: 強気 >=2, 弱気 <= -1）は初期設定。実際の指標でテストし、市場関係者の一般的な解釈と合うように調整する。特定の指標でこのロジックではうまく表現できない場合は、その指標専用の判定ロジックを設けることも検討（ただし、まずは共通ロジックの汎用性を追求）。

4.  **`null`値の扱い**: `actual` は必須とする。`forecast` や `previous` が `null` の場合（データが存在しない場合）のスコア計算ルールを明確にする。上記の例では、`null` の場合はその比較によるスコア加算を行わない。

### 2.6 検証
- [X] 入出力が明確である。
- [X] 指標の方向性（高い方が良い／低い方が良い）が考慮されている。
- [X] 「実績」「予想」「前回」の3値比較のロジックが定義されている。
- [X] スコアリングと閾値による判定方法が具体的である。
- [X] `null`値（データ欠損）の扱いが考慮されている。
- [X] 調整可能性（スコア、閾値）が考慮されている。

🎨🎨🎨 EXITING CREATIVE PHASE: ALGORITHM 🎨🎨🎨
Summary: Algorithms for Market Condition Classification (大中小) and Market Sentiment (強気/普通/弱気) have been designed.
Key Decisions:
- Market Condition: Standard deviation (σ) based thresholds using 5-year historical average volatility.
- Market Sentiment: Scoring system based on comparing actual, forecast, and previous values, considering indicator direction (positive/negative impact of high/low values).
Next Steps: Verify all components have completed creative phases and prepare for IMPLEMENT mode. 
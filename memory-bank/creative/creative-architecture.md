🎨🎨🎨 ENTERING CREATIVE PHASE: ARCHITECTURE 🎨🎨🎨

Focus: System Architecture Design for Economic Indicator Analysis System
Objective: Define robust and scalable architecture for backend API, database, data processing, and frontend state management.
Requirements: Refer to `projectbrief.md` and `tasks.md` (section 1.7).

## 1. バックエンドAPI設計 (Endpoints & Data Formats)

### 1.1 コンポーネント記述
経済指標およびボラティリティデータのCRUD操作、データインポート、統計分析結果の提供を行うRESTful API。

### 1.2 要件と制約
- `projectbrief.md` 3.1.1, 3.1.3, 3.1.4 に記載の機能要件を満たすこと。
- 標準的なHTTPメソッド (GET, POST, PUT, DELETE) を使用。
- JSON形式でリクエストとレスポンスを交換。
- OpenAPI (Swagger) でAPI仕様を記述可能であること。
- Spring Securityによる認証・認可を考慮。

### 1.3 オプション分析

#### オプション1: リソースベース・標準REST API
- **記述**: 各エンティティ（経済指標、ボラティリティデータ）をリソースとし、標準的なREST規約に則ってエンドポイントを設計。
  - 例: `GET /api/indicators`, `POST /api/indicators`, `GET /api/indicators/{id}`
  - インポート: `POST /api/indicators/import`, `POST /api/volatility/import`
  - 分析: `GET /api/analysis/summary?indicatorName=CPI`
- **Pros**:
    - RESTのベストプラクティスに準拠し、理解しやすい。
    - 多くのフレームワークやツールとの親和性が高い。
    - クライアント側の実装が容易になる傾向がある。
- **Cons**:
    - 分析APIのような非CRUD操作のエンドポイント設計に少し工夫が必要な場合がある。
- **技術適合性**: 高
- **複雑性**: 中
- **スケーラビリティ**: 高

#### オプション2: 機能ベースAPI (RPCスタイルに近い)
- **記述**: 操作や機能をベースにエンドポイントを設計。
  - 例: `POST /api/importIndicators`, `POST /api/importVolatilityData`, `GET /api/getAnalysisSummary`
- **Pros**:
    - 特定のユースケースに特化したエンドポイント名で直感的になる場合がある。
- **Cons**:
    - RESTの原則から逸脱する可能性があり、統一性が損なわれることがある。
    - リソースの概念が曖昧になりやすい。
    - クライアントが多くのカスタムエンドポイントを覚える必要があるかもしれない。
- **技術適合性**: 中
- **複雑性**: 中～高（統一性維持のため）
- **スケーラビリティ**: 中～高

### 1.4 推奨アプローチ: オプション1 (リソースベース・標準REST API)
- **理由**: 標準的で理解しやすく、Spring MVCおよびOpenAPIとの親和性が非常に高い。クライアント開発者にとっても予測可能で、ドキュメント化もしやすい。分析APIも `/api/analysis/{type}` のようにリソースとして定義することで対応可能。

### 1.5 実装ガイドライン

**基本パス**: `/api`

**経済指標 (Economic Indicators)**
- `GET /indicators`: 全経済指標リスト取得 (ページネーション対応: `?page=0&size=20&sort=jstTime,desc`)
- `POST /indicators`: 新規経済指標作成
    - Request Body: `EconomicIndicatorDto`
    - Response: `201 Created`, `EconomicIndicatorDto`
- `GET /indicators/{id}`: 特定の経済指標取得
    - Response: `EconomicIndicatorDto`
- `PUT /indicators/{id}`: 特定の経済指標更新
    - Request Body: `EconomicIndicatorDto`
    - Response: `EconomicIndicatorDto`
- `DELETE /indicators/{id}`: 特定の経済指標削除
    - Response: `204 No Content`
- `POST /indicators/import`: 経済指標JSONデータ一括インポート
    - Request Body: `IndicatorsImportDto` (例: `{"indicators": [...]}` - `projectbrief.md` 2.1.1参照)
    - Response: `202 Accepted` or `ImportStatusDto` (インポート結果概要)

**ボラティリティデータ (Volatility Data)**
- `GET /volatility`: 全ボラティリティデータリスト取得 (ページネーション、フィルタ対応: `?pair=EURUSD&date=2025-05-30`)
- `POST /volatility`: 新規ボラティリティデータ作成
    - Request Body: `VolatilityDataDto`
    - Response: `201 Created`, `VolatilityDataDto`
- `GET /volatility/{id}`: 特定のボラティリティデータ取得
- `PUT /volatility/{id}`: 特定のボラティリティデータ更新
- `DELETE /volatility/{id}`: 特定のボラティリティデータ削除
- `POST /volatility/import`: ZigZagボラティリティJSONデータ一括インポート
    - Request Body: `VolatilityImportDto` (例: `{"volatilityData": [...]}` - `projectbrief.md` 2.1.2参照)
    - Response: `202 Accepted` or `ImportStatusDto`

**統計分析 (Analysis)**
- `GET /analysis/summary`: 指定された経済指標の分析サマリー取得
    - Query Params: `indicatorName` (必須), `country` (任意)
    - Response: `AnalysisSummaryDto` (地合い分類、直近2回実績、地合い判定などを含む)
- `GET /analysis/market-condition/{indicatorName}`: 特定指標の5年分平均からの地合い分類取得
    - Response: `MarketConditionDto`
- `GET /analysis/recent-performance/{indicatorName}`: 特定指標の直近2回の実績値取得
    - Response: `List<IndicatorPerformanceDto>`
- `GET /analysis/sentiment/{indicatorName}`: 特定指標の地合い判定取得
    - Response: `SentimentDto`

**データフォーマット (DTOs - Data Transfer Objects)**
- 各エンティティに対応するDTOを定義 (例: `EconomicIndicatorDto`, `VolatilityDataDto`)。
- DTOにはバリデーションアノテーション (`@NotNull`, `@Size`, etc.) を付与。
- 時刻はISO 8601形式の文字列 (例: `2025-05-30T21:30:00Z`)。

```java
// EconomicIndicatorDto.java (例)
public class EconomicIndicatorDto {
    private Long id;
    private String utcTime; // ISO 8601
    private String jstTime; // ISO 8601
    @NotNull @Size(min = 1, max = 100)
    private String name;
    @NotNull @Size(min = 2, max = 2)
    private String country; // e.g., "US"
    private Double actual;
    private Double forecast;
    private Double previous;
    private VolatilityDataDto linkedVolatility;
    private String marketCondition; // "大", "中", "小"
    private String sentiment; // "強気", "普通", "弱気"
}

// VolatilityDataDto.java (例)
public class VolatilityDataDto {
    private Long id;
    @NotNull
    private String date; // ISO 8601 (yyyy-MM-dd)
    @NotNull @Pattern(regexp = "\\d{2}-\\d{2}") // e.g., "07-09"
    private String timeSlot;
    @NotNull @Size(min = 6, max = 6) // e.g., "EURUSD"
    private String pair;
    private Double high;
    private Double low;
    private Double volatility;
}

// AnalysisSummaryDto.java (例)
public class AnalysisSummaryDto {
    private String indicatorName;
    private String country;
    private String marketCondition; // "大", "中", "小"
    private List<IndicatorPerformanceDto> recentPerformances;
    private String sentiment; // "強気", "普通", "弱気"
}

// IndicatorPerformanceDto.java (例)
public class IndicatorPerformanceDto {
    private String time; // JST, or original UTC
    private Double actual;
    private Double forecast;
    private Double previous;
    private Double linkedVolatilityValue; // 紐づいたボラティリティの値
}

// ImportStatusDto.java (例)
public class ImportStatusDto {
    private int totalItems;
    private int importedItems;
    private int failedItems;
    private List<String> errorMessages;
}
```

### 1.6 検証
- [X] `projectbrief.md` の主要なAPI要件をカバーしている。
- [X] 標準的なREST原則に準拠している。
- [X] OpenAPIでの記述が容易である。
- [X] 拡張性がある程度考慮されている (ページネーション、フィルタリング)。

## 2. データベーススキーマ設計 (Tables, Columns, Indexes, Relationships)

### 2.1 コンポーネント記述
経済指標データとボラティリティデータを格納するためのMySQLデータベーススキーマ。

### 2.2 要件と制約
- `projectbrief.md` 4.1 エンティティ設計をベースとする。
- 5年分の履歴データおよび日次データを効率的に格納・検索できること。
- 経済指標とボラティリティデータ間のリレーションシップを定義する。
- 検索パフォーマンス向上のための適切なインデックスを設定する。

### 2.3 オプション分析 (主にリレーションシップとインデックスに関して)

#### オプション1: `EconomicIndicator` から `VolatilityData` への一対一関連 (現状の`projectbrief.md`案)
- **記述**: `EconomicIndicator` エンティティに `linkedVolatility_id` (FK) を持ち、`VolatilityData` テーブルの主キーを参照する。
- **Pros**:
    - 構造がシンプルで理解しやすい。
    - 指標からボラティリティを引くのが容易。
- **Cons**:
    - 一つのボラティリティデータが複数の指標発表（例：同時間帯の別指標）に紐づく可能性を考慮すると、厳密には一対一ではない場合がある。ただし、今回の要件では「指標発表時刻に最も近い時間帯のボラティリティ」という解釈であれば問題ない可能性が高い。
    - `VolatilityData`側からどの指標に紐づいているかを知るには逆方向のクエリが必要。
- **インデックス推奨**:
    - `economic_indicators(jst_time)`: 時系列でのソート・検索用。
    - `economic_indicators(name, country)`: 特定の指標名での検索用。
    - `economic_indicators(linked_volatility_id)`: 結合用(FKには自動で作成される場合もあるが明示)。
    - `volatility_data(date, time_slot, pair)`: 特定の日時・通貨ペアのボラティリティ検索用。

#### オプション2: 中間テーブルを用いた多対多関連 (より柔軟性を持たせる場合)
- **記述**: `indicator_volatility_link` のような中間テーブルを作成し、`economic_indicator_id` と `volatility_data_id` を格納する。
- **Pros**:
    - ボラティリティデータが複数の指標に、あるいは指標が複数のボラティリティ（厳密にはないが）に関連付けられるような、より複雑なシナリオに対応可能。
    - 双方向の参照が容易になる。
- **Cons**:
    - スキーマがやや複雑になる。
    - 今回の要件では過剰設計の可能性がある。
- **インデックス推奨**:
    - オプション1のインデックスに加え、中間テーブルの各FKにインデックス。

### 2.4 推奨アプローチ: オプション1 (`EconomicIndicator` から `VolatilityData` への一対一関連)
- **理由**: 現状の要件定義では、「経済指標発表時刻（JST）→該当時間帯のボラティリティを検索・紐付け」とあり、一つの指標に対して一つの主要な関連ボラティリティデータを紐付ける形が自然。スキーマのシンプルさを優先する。将来的に拡張が必要になった場合は、マイグレーションで対応可能。

### 2.5 実装ガイドライン (MySQL DDL例)

```sql
CREATE TABLE volatility_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    time_slot VARCHAR(5) NOT NULL, -- e.g., "07-09"
    pair VARCHAR(6) NOT NULL,      -- e.g., "EURUSD"
    high DOUBLE,
    low DOUBLE,
    volatility DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_vol_date_slot_pair (date, time_slot, pair) -- 主要な検索キー
);

CREATE TABLE economic_indicators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utc_time DATETIME NOT NULL,     -- 元のUTC時刻
    jst_time DATETIME NOT NULL,     -- JST変換後の時刻
    name VARCHAR(100) NOT NULL,   -- 指標名
    country VARCHAR(2) NOT NULL,    -- 国コード (e.g., "US")
    actual DOUBLE,
    forecast DOUBLE,
    previous DOUBLE,
    linked_volatility_id BIGINT NULL, -- 紐づくボラティリティデータのID
    market_condition VARCHAR(10) NULL, -- 地合い分類 ("大", "中", "小")
    sentiment VARCHAR(20) NULL,      -- 地合い判定 ("強気", "普通", "弱気")
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (linked_volatility_id) REFERENCES volatility_data(id),
    INDEX idx_ei_jst_time (jst_time DESC), -- 時系列でのソート・検索
    INDEX idx_ei_name_country (name, country) -- 特定指標の検索
);
```

**カラム詳細**: `projectbrief.md` 4.1 エンティティ設計を参照しつつ、DB標準的な命名規則 (`snake_case`) と型、`NOT NULL`制約、デフォルトタイムスタンプ (`created_at`, `updated_at`) を追加。

**データ型について**:
- `DATETIME` (MySQL): Javaの`LocalDateTime`に対応。タイムゾーン情報はアプリケーション側でUTC/JSTを明確に扱う。
- `VARCHAR(2)` for `country`: ISO 3166-1 alpha-2国コードを想定。
- `VARCHAR(100)` for `name`: 指標名に適度な長さを確保。

### 2.6 検証
- [X] `projectbrief.md` のエンティティ設計が反映されている。
- [X] 主要な検索パターン（時系列、指標名）に対応するインデックスが提案されている。
- [X] リレーションシップ（一対一）が定義されている。
- [X] データ型と制約が適切である。

## 3. 大量データ処理戦略 (5年分の履歴データ)

### 3.1 コンポーネント記述
初期データとして5年分の経済指標およびボラティリティデータをインポートし、その後の統計計算（平均ボラティリティ算出など）を効率的に行うための戦略。

### 3.2 要件と制約
- 初期インポート処理が現実的な時間内に完了すること (例: 数分～数十分以内)。
- インポート処理中にシステムが長時間利用不可にならないこと（理想）。
- 統計計算（特に5年分平均）が定期的に、または必要に応じて効率的に実行できること。
- DBのパフォーマンスに過度な負荷をかけないこと。

### 3.3 オプション分析

#### オプション1: アプリケーション層での一括バッチ処理 (Spring Batchなど)
- **記述**: JSONファイルを読み込み、Spring Batchのようなフレームワークを利用してチャンクごとにデータを処理し、DBに永続化する。統計計算もバッチ処理として実装。
- **Pros**:
    - Springエコシステム内で完結でき、トランザクション管理やエラーハンドリング、リトライなどが容易。
    - 大量データ処理に特化した機能（チャンク処理、並列処理など）が利用可能。
    - Javaでの実装なので、複雑なビジネスロジックも組み込みやすい。
- **Cons**:
    - Spring Batchの学習コストが若干必要。
    - 設定がやや煩雑になる場合がある。
- **インポート処理**: API経由でファイルアップロード後、非同期でバッチジョブを実行。
- **統計計算**: 定期実行のバッチジョブ、または特定のトリガーで起動。

#### オプション2: DBのロード機能 + ストアドプロシージャ/アプリケーションロジック
- **記述**: MySQLの`LOAD DATA INFILE`等でJSON (またはCSVに変換して) を一時テーブルに高速ロード。その後、SQLやストアドプロシージャ、またはアプリケーションのSQL実行で本テーブルにマージ・加工する。統計計算もSQLクエリやアプリケーション側の処理で実行。
- **Pros**:
    - `LOAD DATA INFILE`は非常に高速。
    - 単純なデータロードであればSQL中心で記述可能。
- **Cons**:
    - JSONのままでは`LOAD DATA INFILE`が直接使えないため、前処理 (CSV変換など) が必要になる場合がある (MySQL 8.0以降は`JSON_TABLE`などで対応可能だが複雑)。
    - 複雑な変換ロジックやエラーハンドリングがSQLだけでは難しい場合がある。
    - ストアドプロシージャはテストやバージョン管理が煩雑になることがある。
- **インポート処理**: ファイルを特定の場所に配置し、DBの機能でロード、その後アプリケーションから追加処理。
- **統計計算**: SQLクエリの最適化が重要。

#### オプション3: 逐次API呼び出しによるインポート (非推奨)
- **記述**: 外部スクリプト等から一件ずつ `POST /api/indicators` のようなAPIを呼び出してデータを投入。
- **Pros**:
    - 既存APIをそのまま使える。
- **Cons**:
    - 非常に低速で、大量データには全く不向き。ネットワークオーバーヘッドが大きい。
    - トランザクション管理が困難。

### 3.4 推奨アプローチ: オプション1 (アプリケーション層での一括バッチ処理 - Spring Batch)
- **理由**: 堅牢性、管理のしやすさ、Springエコシステムとの親和性を考慮。特に、経済指標とボラティリティの紐付け、JST変換などのアプリケーションロジックがインポート処理に含まれるため、Javaで一元的に扱えるメリットが大きい。統計計算も同様に、複雑な集計やデータ操作がJavaで柔軟に実装できる。
    - **初期インポート**: 専用の管理APIエンドポイント (`POST /api/admin/import-historical-data`) を設け、アップロードされたJSONファイルを非同期のSpring Batchジョブで処理する。ジョブのステータス確認APIも提供。
    - **統計計算**: 5年分の平均ボラティリティなどは、アプリケーション起動時や日次バッチ（もし日次更新で平均が変わるなら）で計算し、結果を専用のテーブルやカラムにキャッシュしておくことを検討。リアルタイム計算は負荷が高いため避ける。

### 3.5 実装ガイドライン

**初期データインポート (Spring Batch)**
1.  **Job定義**: `historicalDataImportJob`
    *   **Step 1: `volatilityDataImportStep`**: ボラティリティJSONを読み込み、`VolatilityData`エンティティに変換してDBに保存。
        *   Reader: `JsonItemReader<VolatilityDataJsonItem>` (カスタム)
        *   Processor: `VolatilityDataProcessor` (JSON Item -> `VolatilityData` Entity変換、バリデーション)
        *   Writer: `JpaItemWriter<VolatilityData>`
    *   **Step 2: `economicIndicatorImportStep`**: 経済指標JSONを読み込み、`EconomicIndicator`エンティティに変換。`VolatilityData`との紐付けロジックを実行しDBに保存。
        *   Reader: `JsonItemReader<EconomicIndicatorJsonItem>` (カスタム)
        *   Processor: `EconomicIndicatorProcessor` (JSON Item -> `EconomicIndicator` Entity変換、JST変換、`VolatilityLinkingService`利用、バリデーション)
        *   Writer: `JpaItemWriter<EconomicIndicator>`
2.  **ファイル処理**: アップロードされたファイルは一時領域に保存し、バッチ処理後に削除またはアーカイブ。
3.  **非同期実行**: `@Async` と `JobLauncher` を使用してAPIリクエストは即時応答し、バッチはバックグラウンドで実行。
4.  **エラーハンドリング**: スキップポリシー、リトライポリシーを設定。失敗したアイテムはログに記録。

**統計計算 (平均ボラティリティ等)**
1.  **計算タイミング**: アプリケーション起動時、または専用のバッチジョブとしてスケジュール実行（例：毎日深夜）。または、必要に応じて手動トリガー可能な管理API。
2.  **計算ロジック**: `EconomicIndicatorRepository` や `VolatilityDataRepository` を使用して過去データを集計。
    *   例: `SELECT pair, AVG(volatility) FROM volatility_data WHERE date BETWEEN ? AND ? GROUP BY pair;` のようなクエリをベースに、指標ごとの時間帯などを考慮。
3.  **結果の保存**: 計算結果（例：指標名ごとの平均ボラティリティ）は、`EconomicIndicator`テーブルの`market_condition`算出の元データとして、別テーブル (`average_volatilities`) や `EconomicIndicator` 自体にカラムを追加してキャッシュすることを検討。これにより表示時のパフォーマンスを向上。
    *   `average_volatilities`テーブル案:
        ```sql
        CREATE TABLE average_volatilities (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            indicator_name VARCHAR(100) NOT NULL,
            country VARCHAR(2) NOT NULL,
            pair VARCHAR(6) NULL, -- 通貨ペア（ボラティリティに依存する場合）
            time_slot VARCHAR(5) NULL, -- 時間帯（ボラティリティに依存する場合）
            average_value DOUBLE NOT NULL,
            calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            UNIQUE KEY uq_avg_vol (indicator_name, country, pair, time_slot) 
        );
        ```

### 3.6 検証
- [X] インポート処理がバッチ処理として提案されている。
- [X] 統計計算の効率化（キャッシュ等）が考慮されている。
- [X] Spring Batchの利用が具体的である。
- [X] エラーハンドリングと非同期実行が言及されている。

## 4. フロントエンド状態管理戦略 (React + TypeScript)

### 4.1 コンポーネント記述
React (TypeScript) で構築されるシングルページアプリケーション (SPA) の状態管理戦略。
管理対象の状態: 経済指標リスト、選択された指標詳細、ボラティリティデータ、分析結果、UIの状態（ローディング中、エラー表示など）、ユーザー入力。

### 4.2 要件と制約
- コンポーネント間で状態を効率的に共有できること。
- APIからの非同期データ取得と状態更新を管理しやすいこと。
- 状態の変更が予測可能で、デバッグしやすいこと。
- アプリケーションの規模（中程度）に適した複雑性であること。
- TypeScriptとの親和性が高いこと。

### 4.3 オプション分析

#### オプション1: React Context API + `useReducer` / `useState`
- **記述**: React組み込みのContext APIを利用してグローバルに近い状態を提供。複雑な状態ロジックは`useReducer`、単純なものは`useState`で管理。必要に応じてカスタムフックでラップする。
- **Pros**:
    - 追加ライブラリ不要（React標準機能）。バンドルサイズへの影響が最小限。
    - 小～中規模のアプリケーションでは十分な場合が多い。
    - TypeScriptとの相性は良い。
    - 学習コストが比較的低い（Reactの知識があれば）。
- **Cons**:
    - 大量の高頻度更新がある場合、Contextの再レンダリング最適化に工夫が必要（`memo`など）。
    - 非常に大規模で複雑な状態ロジックになると、管理が煩雑になる可能性。
    - Redux DevToolsのような強力なデバッグツールは標準では提供されない。

#### オプション2: Redux Toolkit (RTK)
- **記述**: Reduxの公式推奨ツールキット。ボイラープレートを大幅に削減し、非同期処理 (thunk) やイミュータブルな更新を容易にするユーティリティを提供。
- **Pros**:
    - 大規模アプリケーションでの状態管理における実績と安定性。
    - Redux DevToolsによる強力なデバッグ機能。
    - イミュータブルな状態更新が強制されやすく、予測可能性が高い。
    - TypeScriptサポートが手厚い (`configureStore`, `createSlice`など)。
    - 非同期処理のための`createAsyncThunk`が便利。
- **Cons**:
    - 小規模なアプリケーションには過剰装備になる可能性がある。
    - Reduxの基本概念（Action, Reducer, Store）の理解が必要（RTKが簡略化しているとはいえ）。
    - バンドルサイズが若干増加。

#### オプション3: Zustand
- **記述**: Reduxにインスパイアされた、よりシンプルで軽量な状態管理ライブラリ。Fluxアーキテクチャに厳密に従わず、フックベースで直感的に利用可能。
- **Pros**:
    - APIが非常にシンプルで学習コストが低い。
    - ボイラープレートが少ない。
    - Context APIのような再レンダリングの懸念が少ない（セレクタによる最適化が容易）。
    - TypeScriptサポートが良い。
    - Redux DevToolsも利用可能。
- **Cons**:
    - Reduxほどの巨大なコミュニティやエコシステムはない（ただし成長中）。
    - ミドルウェアの選択肢はReduxほど多くない。

#### オプション4: TanStack Query (旧 React Query) + Context/Zustand
- **記述**: サーバー状態管理（APIからのデータフェッチ、キャッシュ、同期など）をTanStack Queryに任せ、クライアント固有のUI状態やグローバル設定のみをContext APIやZustandで管理する。
- **Pros**:
    - サーバー状態とクライアント状態の関心事を明確に分離できる。
    - TanStack Queryがキャッシュ、バックグラウンド更新、リトライ、楽観的更新など、非同期データ処理に関する多くの複雑な問題を解決してくれる。
    - APIローディング/エラー状態の管理が大幅に簡略化。
    - 結果として、クライアント側の状態管理ロジックがシンプルになる。
- **Cons**:
    - TanStack Queryの学習コストが別途必要。
    - クライアント状態管理のために別途ライブラリ（ContextまたはZustandなど）も組み合わせる必要がある。

### 4.4 推奨アプローチ: オプション4 (TanStack Query + Zustand)
- **理由**: このプロジェクトはAPIからのデータ取得・表示が中心であり、TanStack Queryはそのようなサーバー状態管理に非常に強力。キャッシュやバックグラウンド更新によりUX向上も期待できる。クライアント固有のUI状態（例：モーダルの表示状態、選択中のフィルターなど）は数が少ないと予想され、それらは軽量なZustandで管理するのがバランスが良いと判断。両者はTypeScriptとの相性も良く、開発効率とパフォーマンスの両立が期待できる。
    - **TanStack Query**: APIリクエスト、レスポンスのキャッシュ、ローディング/エラー状態の管理、データの同期を担当。
    - **Zustand**: UIテーマ、ユーザー設定、一時的なフォームの状態（複雑な場合）など、純粋なクライアント側状態を管理。

### 4.5 実装ガイドライン

**1. TanStack Query (`@tanstack/react-query`) の設定**
   - `QueryClientProvider` をアプリケーションのルート近くに配置。
   - API呼び出しごとにカスタムフックを作成 (例: `useEconomicIndicators`, `useVolatilityData`)。
     ```typescript
     // src/hooks/useEconomicIndicators.ts (例)
     import { useQuery } from '@tanstack/react-query';
     import { apiClient } from '../services/apiClient'; // axios instance
     import { EconomicIndicatorDto } from '../types/dto'; // DTO型定義

     const fetchEconomicIndicators = async (params) => {
         const { data } = await apiClient.get<EconomicIndicatorDto[]>('/indicators', { params });
         return data;
     };

     export const useEconomicIndicators = (params) => {
         return useQuery(['economicIndicators', params], () => fetchEconomicIndicators(params), {
             // staleTime, cacheTime などのオプションを設定可能
         });
     };
     ```
   - `useMutation` をデータ更新系API (POST, PUT, DELETE) に使用。

**2. Zustand の設定**
   - 管理する状態ごとにストアを作成 (例: `uiStore`, `filterStore`)。
     ```typescript
     // src/stores/uiStore.ts (例)
     import create from 'zustand';

     interface UIState {
         isSidebarOpen: boolean;
         toggleSidebar: () => void;
         theme: 'light' | 'dark';
         setTheme: (theme: 'light' | 'dark') => void;
     }

     export const useUIStore = create<UIState>((set) => ({
         isSidebarOpen: true,
         toggleSidebar: () => set((state) => ({ isSidebarOpen: !state.isSidebarOpen })),
         theme: 'light',
         setTheme: (theme) => set({ theme }),
     }));
     ```
   - コンポーネント内でフック (`useUIStore`) を使用して状態にアクセス・更新。

**3. APIクライアント (`axios`)**
   - ベースURL、共通ヘッダー、エラーハンドリングなどを設定した`axios`インスタンスを作成 (`src/services/apiClient.ts`)。
   - TanStack Queryのクエリ関数内でこのインスタンスを利用。

**4. 型定義**
   - APIレスポンスや状態の型定義を `src/types/` ディレクトリなどに集約。

### 4.6 検証
- [X] 主要な状態管理の対象が明確になっている。
- [X] 非同期処理 (API連携) が効率的に扱える戦略である。
- [X] TypeScriptとの親和性が高い。
- [X] アプリケーションの規模と複雑性に適している。
- [X] サーバー状態とクライアント状態の分離が考慮されている。

🎨🎨🎨 EXITING CREATIVE PHASE: ARCHITECTURE 🎨🎨🎨
Summary: Backend API, Database Schema, Large Data Handling, and Frontend State Management strategies have been defined.
Key Decisions:
- Backend API: Resource-based standard REST API.
- Database: Single table for Economic Indicators and Volatility Data with a one-to-one link, using MySQL.
- Large Data Handling: Spring Batch for initial import and potentially for statistical calculations.
- Frontend State Management: TanStack Query for server state and Zustand for client state.
Next Steps: Proceed to Algorithm Design creative phase. 
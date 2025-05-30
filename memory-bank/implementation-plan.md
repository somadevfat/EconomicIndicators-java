# Implementation Plan

## 概要
このドキュメントは、経済指標・ボラティリティ分析システムの開発における詳細な実装計画を記述します。
`tasks.md` の計画に基づき、クリエイティブフェーズで決定された設計詳細を反映しています。

## 1. バックエンドAPI開発 (Spring Boot)

### 1.1 環境構築・プロジェクト初期設定
- Spring Bootプロジェクト作成 (Spring Initializr)
- 必要な依存関係を`pom.xml`に追加 (Web, Data JPA, MySQL Driver, Security, Lombok, Validation, OpenAPI, Spring Batch)
- Dockerfile (`backend`) 作成
- `application.properties` もしくは `application.yml` 設定 (DB接続情報、JSTタイムゾーン設定等)

### 1.2 データモデル設計とエンティティ作成
- **エンティティ**:
    - `EconomicIndicator.java`
    - `VolatilityData.java`
    - `AverageVolatility.java` (統計計算結果キャッシュ用 - `creative-architecture.md` 3.5 参照)
- **JPAエンティティ定義**: `creative-architecture.md` のデータベーススキーマ設計に基づき定義。
    - `id`, `createdAt`, `updatedAt` などの共通カラムを含む。
    - リレーションシップ: `EconomicIndicator` から `VolatilityData` へは `OneToOne` (NULL許容)。
- **リポジトリインターフェース作成** (Spring Data JPA)

### 1.3 データインポート機能実装 (Spring Batch)
- **参照**: `creative-architecture.md` セクション3 (大量データ処理戦略)
- **ジョブ定義**: `historicalDataImportJob`
    - `volatilityDataImportStep`: ボラティリティJSON -> `VolatilityData`エンティティ -> DB
    - `economicIndicatorImportStep`: 経済指標JSON -> `EconomicIndicator`エンティティ (JST変換、ボラティリティ紐付け) -> DB
- **APIエンドポイント**: `POST /api/admin/import-historical-data` (非同期バッチ起動)
- UTCからJSTへの時刻変換ロジック (`ZoneId.of("Asia/Tokyo")` を使用)
- 時間帯ベースでの経済指標とボラティリティの紐付けロジック (`VolatilityLinkingService.java`):
    - 経済指標の `jstTime` を基に、`projectbrief.md` 2.1.3 の時間帯区分に合致する `VolatilityData` を検索しリンクする。最も近いものを選択。

### 1.4 統計分析機能実装
- **5年分平均ボラティリティ計算ロジック**:
    - `AverageVolatilityService.java`
    - Spring Batchジョブまたはアプリケーション起動時に計算し、`AverageVolatility` テーブルにキャッシュ。
    - 指標名、国、(通貨ペア、時間帯) ごとに平均値と標準偏差を計算。
- **地合い3分類アルゴリズム実装**: (`MarketConditionService.java`)
    - `creative-algorithms.md` セクション1のロジックに基づき実装。
    - 入力: `currentVolatility`, `avgVolatility5y`, `stdDevVolatility5y` (キャッシュから取得)。
    - 出力: "大", "中", "小"。
- **直近2回データ取得ロジック**: `EconomicIndicatorRepository` で `jstTime` を降順ソートし、上位2件を取得。
- **地合い判定ロジック**: (`MarketSentimentService.java`)
    - `creative-algorithms.md` セクション2のロジックに基づき実装。
    - 入力: `actual`, `forecast`, `previous`, `IndicatorDirection`。
    - 出力: "強気", "普通", "弱気"。

### 1.5 REST APIエンドポイント実装
- **参照**: `creative-architecture.md` セクション1 (バックエンドAPI設計)
- **コントローラ**:
    - `EconomicIndicatorController.java`:
        - `GET /indicators`: ページネーション対応 (`Pageable`)
        - `POST /indicators`
        - `GET /indicators/{id}`
        - `PUT /indicators/{id}`
        - `DELETE /indicators/{id}`
        - `POST /indicators/import`: `IndicatorsImportDto` (内部的に上記バッチ処理とは別の一括登録用。少量データ用か、バッチを呼び出すかは要検討。現時点ではバッチが主。)
    - `VolatilityDataController.java`:
        - `GET /volatility`: ページネーション、フィルタ対応
        - `POST /volatility`
        - `GET /volatility/{id}`
        - `PUT /volatility/{id}`
        - `DELETE /volatility/{id}`
        - `POST /volatility/import`: `VolatilityImportDto`
    - `AnalysisController.java`:
        - `GET /analysis/summary?indicatorName=&country=` -> `AnalysisSummaryDto`
        - `GET /analysis/market-condition/{indicatorName}` -> `MarketConditionDto`
        - `GET /analysis/recent-performance/{indicatorName}` -> `List<IndicatorPerformanceDto>`
        - `GET /analysis/sentiment/{indicatorName}` -> `SentimentDto`
    - `AdminController.java`: (Spring SecurityでADMINロール保護)
        - `POST /admin/import-historical-data`: 上記のSpring Batchジョブ起動
        - `POST /admin/recalculate-average-volatility`: 平均ボラティリティ再計算ジョブ起動
- **DTOs**: `creative-architecture.md` で定義されたDTO (`EconomicIndicatorDto`, `VolatilityDataDto`, `AnalysisSummaryDto` 等) を作成。バリデーションアノテーション付与。

### 1.6 セキュリティと品質管理
- Spring Security設定 (JWT認証ベース - 詳細は別途設計・実装)
- Bean Validationを用いた入力検証 (`@Valid` アノテーション)
- GlobalExceptionHandler (`@ControllerAdvice`) による共通例外処理
- OpenAPI/Swaggerドキュメント生成・整備 (`springdoc-openapi-ui`)

### 1.7 単体テスト・結合テスト
- 各サービスクラスの単体テスト (JUnit, Mockito)
- APIエンドポイントの結合テスト (Spring MVC Test, Testcontainers for DB)

## 2. フロントエンド開発 (React + TypeScript)

### 2.1 環境構築・プロジェクト初期設定
- Reactプロジェクト作成 (Create React App with TypeScript or Vite)
- 必要なライブラリインストール: `axios`, `react-router-dom`, `@tanstack/react-query`, `zustand`
- Dockerfile (`frontend`) 作成

### 2.2 API連携と状態管理
- **参照**: `creative-architecture.md` セクション4 (フロントエンド状態管理戦略)
- **APIクライアント**: `axios` インスタンス設定 (`src/services/apiClient.ts`)
- **サーバー状態管理**: `@tanstack/react-query`
    - API呼び出しごとのカスタムフック (`useEconomicIndicators`, `useAnalysisSummary` 等)
    - キャッシュ戦略、ローディング・エラー状態管理
- **クライアント状態管理**: `zustand`
    - UI状態 (テーマ、モーダル表示等)、選択中フィルタ等のグローバル状態を管理 (`src/stores/`)

### 2.3 基本的な画面レイアウト・ルーティング設定
- ヘッダー、サイドバー（ナビゲーション）、フッター等の共通レイアウトコンポーネント作成。
- `react-router-dom` による画面遷移設定:
    - `/` (メイン表示画面)
    - `/indicators` (経済指標一覧・管理)
    - `/indicators/:id` (経済指標詳細)
    - `/volatility` (ボラティリティデータ一覧・管理)
    - `/import` (データインポート画面)
    - `/admin` (管理機能トップ - 将来的な拡張用)

### 2.4 メイン表示画面実装
- `projectbrief.md` 3.2.1参照
- 経済指標一覧 (フィルタ、ソート、ページネーション)
    - 各指標の主要情報 (名称、国、JST時刻、実績/予想/前回)
    - 地合い分類表示 (大/中/小 - 色分けやアイコンで視覚的に)
    - 地合い判定表示 (強気/普通/弱気 - 色分けやアイコンで視覚的に)
    - 直近2回実績表示
    - 関連ボラティリティ表示
- クリックで詳細表示モーダルまたは別ページへ遷移

### 2.5 管理機能画面実装
- `projectbrief.md` 3.2.2参照
- **データインポート画面**:
    - 経済指標JSONファイルアップロード
    - ボラティリティJSONファイルアップロード
    - インポート実行ボタン (バックエンドの `/admin/import-historical-data` を呼び出し)
    - インポート状況表示 (進捗、結果)
- **経済指標一覧・管理画面**: CRUD操作インターフェース (API連携)
- **ボラティリティデータ一覧・管理画面**: CRUD操作インターフェース (API連携)
- 統計再計算トリガーボタン (例: `/admin/recalculate-average-volatility` 呼び出し)

### 2.6 UIテスト
- 主要コンポーネントの単体テスト (Jest, React Testing Library)

## 3. Docker化と総合テスト

### 3.1 Docker Compose設定
- `docker-compose.yml` 作成 (バックエンド、フロントエンド、MySQL)
- 各サービスのネットワーク設定、ボリューム設定 (DBデータ永続化等)
- 環境変数設定

### 3.2 システム全体の起動と動作確認
- `docker-compose up --build` による全サービス起動
- フロントエンドからバックエンドAPIへの接続確認
- データインポートから表示までの一連のフローテスト

### 3.3 最終テスト・デバッグ
- クロスブラウザテスト (主要ブラウザ)
- パフォーマンステスト (特にデータ表示部分)

## 4. その他
- **ログ戦略**: バックエンドはSLF4J + Logback。フロントエンドは `console.log` を基本とし、必要に応じてSentry等のエラー監視サービス導入検討。
- **バージョン管理**: Git (GitHub/GitLab等)

## 5. デプロイ・運用・品質向上

### 5.1 クラウドデプロイ
- AWS (ECS/Fargate, ECR) や GCP (Cloud Run, GKE, Container Registry) へのコンテナデプロイ手順を用意し、Infrastructure as Code (Terraform, CloudFormation) による構成管理を行う。
- `docker-compose.yml` や Kubernetes マニフェストをリポジトリに含め、GitHub Actions などのCIツールから自動デプロイできる設定を整備。

### 5.2 テストコードの充実
- 単体テスト: JUnit 5, Mockito を用いてサービス層、リポジトリ層のテストを網羅。
- 結合テスト: Spring Boot Test を用いてコントローラやDB接続を含む統合テストを実装。
- テストカバレッジ: JaCoCo を導入し、カバレッジレポートを生成、Codecov 等へのアップロードをCIで自動化。

### 5.3 パフォーマンスへの配慮
- 大量データ処理を想定し、JMH や Gatling でベンチマークを作成。
- プロファイリング: VisualVM, Flight Recorder 等でボトルネックを解析し、DBインデックスやキャッシュ (Redis/Caffeine) の利用を検討。
- 非同期処理: Spring Batch、ExecutorService、WebFlux など、必要に応じた非同期パターンを採用。

### 5.4 CI/CD パイプライン構築
- GitHub Actions (または GitLab CI) で以下を自動化:
  1. コードチェックアウト
  2. `mvn test` で単体・統合テスト実行
  3. JaCoCo レポート生成
  4. Docker イメージビルド & セキュリティスキャン (Trivy, Snyk)
  5. コンテナレジストリ (Docker Hub, ECR, GCR) へのプッシュ
  6. クラウド環境へのデプロイ (ECS Task 定義 or kubectl apply)
- パイプライン設定ファイル (`.github/workflows/ci.yml`) のサンプルをリポジトリに追加。

この実装計画は、開発の進捗や新たな発見に応じて更新される可能性があります。 
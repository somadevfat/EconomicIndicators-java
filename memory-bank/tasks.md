# Tasks

- [x] Initialize project
- [x] Gitflow に基づくブランチ運用設定
- [x] Plan project (Level 3)

## 1. プロジェクト計画 (Level 3)

### 1.1 タスク名
経済指標・ボラティリティ分析システム開発

### 1.2 説明
MT5から出力された経済指標とZigZagボラティリティデータを分析し、地合い判定と統計分析を行うバックエンドAPIおよびフロントエンド管理画面を開発する。

### 1.3 複雑度
- レベル: 3 (中間機能)
- タイプ: 新規システム開発

### 1.4 技術スタック
- バックエンド: Spring Boot (Java)
- フロントエンド: React (TypeScript)
- データベース: MySQL
- コンテナ化: Docker
- ビルドツール: Maven (バックエンド), npm/yarn (フロントエンド)
- API仕様: OpenAPI (Swagger)

### 1.5 技術スタック検証チェックポイント
- [ ] Spring Bootプロジェクト初期化コマンド検証済み
- [ ] Reactプロジェクト初期化コマンド検証済み
- [ ] Docker Desktop/Engineインストールおよび動作確認済み
- [ ] MySQL Dockerコンテナ起動および接続確認済み
- [ ] 各主要ライブラリ (Spring Security, Spring Data JPA, etc.) の互換性調査済み
- [ ] フロントエンド・バックエンド間の基本的な通信 (Hello World API) 検証済み
- [ ] OpenAPI/Swagger UI表示および基本的なAPI定義検証済み

### 1.6 実装計画

#### 1.6.1 フェーズ1: バックエンドAPI開発 (Spring Boot)
1.  **[x] 環境構築・プロジェクト初期設定**
    *   [x] Spring Bootプロジェクト作成 (Spring Initializr)
    *   [x] 必要な依存関係を`pom.xml`に追加 (Web, Data JPA, MySQL Driver, Security, Lombok, Validation, OpenAPI)
    *   [x] Dockerfile (`backend`) 作成
    *   [x] `application.properties` もしくは `application.yml` 設定 (DB接続情報等)
2.  **[x] データモデル設計とエンティティ作成** (`EconomicIndicator.java`, `VolatilityData.java` - `projectbrief.md`参照)
    *   [x] `EconomicIndicator`エンティティ定義 (id, utcTime, jstTime, name, country, actual, forecast, previous, linkedVolatility, marketCondition, sentiment)
    *   [x] `VolatilityData`エンティティ定義 (id, date, timeSlot, pair, high, low, volatility)
    *   [x] リポジトリインターフェース作成 (Spring Data JPA)
3.  **[ ] データインポート機能実装**
    *   [x] 経済指標JSONインポートロジック (`POST /api/indicators/import`)
        *   [ ] JSONパース処理
        *   [ ] DB保存処理
    *   [x] ZigZagボラティリティJSONインポートロジック (`POST /api/volatility/import`)
        *   [ ] JSONパース処理
        *   [ ] DB保存処理
    *   [x] UTCからJSTへの時刻変換ロジック (`java.time.ZoneId`, `ZonedDateTime` 等を利用)
    *   [ ] 時間帯ベースでの経済指標とボラティリティの紐付けロジック (`VolatilityLinkingService.java`)
        *   [ ] JST時刻から該当する時間帯区分を特定する処理
        *   [ ] 該当する時間帯のボラティリティデータを検索する処理
        *   [ ] `EconomicIndicator`エンティティに`linkedVolatility`を設定して保存する処理
    *   [ ] 5年分履歴データ一括インポート機能
        *   [ ] 大容量JSONファイルの効率的な処理方法検討（ストリーム処理など）
4.  **[ ] 統計分析機能実装**
    *   [x] 5年分平均ボラティリティ計算ロジック (通貨ペア別、時間帯別)
    *   [ ] 地合い3分類アルゴリズム実装 (「大」「中」「小」の3分類、具体的な閾値設定)
    *   [ ] 直近2回データ取得ロジック (同一指標名、同一通貨ペアの過去データを新しい順に2件取得)
    *   [ ] 地合い判定ロジック (直近2回の実績値の平均と、5年分平均ボラティリティを比較し、ずれ具合で判定)
5.  **[ ] REST APIエンドポイント実装** (CRUD操作含む - `projectbrief.md` 3.1.4参照)
    *   [ ] `EconomicIndicatorController`
        *   [ ] `POST /api/indicators`: 経済指標作成
        *   [ ] `GET /api/indicators`: 経済指標一覧取得
        *   [ ] `GET /api/indicators/{id}`: 経済指標詳細取得
        *   [ ] `PUT /api/indicators/{id}`: 経済指標更新
        *   [ ] `DELETE /api/indicators/{id}`: 経済指標削除
        *   [ ] `POST /api/indicators/import`: 経済指標JSONインポート (既存タスクと統合)
    *   [ ] `VolatilityDataController`
        *   [ ] `POST /api/volatility`: ボラティリティデータ作成
        *   [ ] `GET /api/volatility`: ボラティリティデータ一覧取得
        *   [ ] `GET /api/volatility/{id}`: ボラティリティデータ詳細取得
        *   [ ] `PUT /api/volatility/{id}`: ボラティリティデータ更新
        *   [ ] `DELETE /api/volatility/{id}`: ボラティリティデータ削除
        *   [ ] `POST /api/volatility/import`: ZigZagボラティリティJSONインポート (既存タスクと統合)
    *   [ ] `AnalysisController`
        *   [ ] `GET /api/analysis/summary`: 統計分析結果（地合い分類、直近2回実績、地合い判定）取得 (指標ごと)
6.  **[ ] セキュリティと品質管理**
    *   [ ] Spring Security設定 (JWT認証ベース)
    *   [ ] Bean Validationを用いた入力検証
    *   [ ] GlobalExceptionHandlerによる例外処理
    *   [ ] OpenAPI/Swaggerドキュメント生成・整備
7.  **[ ] 単体テスト・結合テスト**
    *   [ ] 各サービスクラスの単体テスト (JUnit, Mockito)
    *   [ ] APIエンドポイントの結合テスト (Spring MVC Test)

#### 1.6.2 フェーズ2: フロントエンド開発 (React + TypeScript)
1.  **[ ] 環境構築・プロジェクト初期設定**
    *   [ ] Reactプロジェクト作成 (Create React App with TypeScript)
    *   [ ] 必要なライブラリインストール (axios, react-router-dom, date-fns, chart.js等 UI表示ライブラリ)
    *   [ ] Dockerfile (`frontend`) 作成
2.  **[ ] 基本的な画面レイアウト・ルーティング設定**
    *   [ ] ヘッダー、サイドバー、フッター等の共通コンポーネント作成
    *   [ ] React Routerによる画面遷移設定
        *   [ ] `/dashboard` (メイン表示画面)
        *   [ ] `/admin/import` (データインポート画面)
        *   [ ] `/admin/indicators` (経済指標一覧・詳細管理画面)
        *   [ ] `/admin/analysis` (統計分析結果表示画面)
3.  **[ ] メイン表示画面実装 (`/dashboard`)** (`projectbrief.md` 3.2.1参照)
    *   [ ] API連携 (バックエンドから `/api/analysis/summary` を呼び出しデータ取得)
    *   [ ] 経済指標一覧表示コンポーネント
        *   [ ] 指標名、国、地合い分類（大中小）を表示
        *   [ ] 各指標クリックで詳細表示モーダルまたは別画面へ遷移
    *   [ ] 各指標の詳細情報表示
        *   [ ] 地合い分類：「大」「中」「小」
        *   [ ] 直近2回実績：過去2回の発表値と、その時の紐付けられたボラティリティ
        *   [ ] 地合い判定：「強気」「普通」「弱気」等（具体的な文言は要検討）
4.  **[ ] 管理機能画面実装** (`projectbrief.md` 3.2.2参照)
    *   [ ] データインポート画面 (`/admin/import`)
        *   [ ] 経済指標JSONアップロードフォーム
        *   [ ] ZigZagボラティリティJSONアップロードフォーム
        *   [ ] アップロード実行ボタン (API: `/api/indicators/import`, `/api/volatility/import`)
        *   [ ] インポート結果表示 (成功/失敗、エラーメッセージ等)
    *   [ ] 経済指標一覧・詳細表示管理画面 (`/admin/indicators`)
        *   [ ] 経済指標一覧テーブル (ページネーション、ソート、フィルタ機能)
        *   [ ] 詳細表示・編集モーダル (CRUD操作: API連携)
    *   [ ] 統計分析結果表示画面 (`/admin/analysis`)
        *   [ ] 指標ごとの統計情報（平均ボラティリティ等）を表示
        *   [ ] 必要に応じてグラフ等で可視化
5.  **[ ] API連携と状態管理**
    *   [ ] APIクライアントモジュール作成 (axios)
    *   [ ] 状態管理ライブラリ導入検討 (Context API, Redux Toolkit, Zustandなど) と実装
6.  **[ ] UIテスト**
    *   [ ] 主要コンポーネントの単体テスト (Jest, React Testing Library)

#### 1.6.3 フェーズ3: Docker化と総合テスト
1.  **[x] Docker Compose設定**
    *   [x] `docker-compose.yml`作成 (バックエンド、フロントエンド、MySQL)
    *   [x] 各サービスのネットワーク設定、ボリューム設定
2.  **[ ] システム全体の起動と動作確認**
    *   [ ] `docker-compose up`による全サービス起動
    *   [ ] フロントエンドからバックエンドAPIへの接続確認
    *   [ ] データインポートから表示までの一連のフローテスト
3.  **[ ] 最終テスト・デバッグ**

#### 1.6.4 フェーズ4: デプロイ・運用・品質向上
1.  **[ ] クラウドデプロイ戦略検討・実施 (AWS/GCP 無料枠)**
    *   [ ] デプロイ先プラットフォーム選定 (例: AWS ECS Fargate, GCP Cloud Run)
    *   [ ] コンテナレジストリ (ECR, GCR, Docker Hub) の利用準備
    *   [ ] IaC (Terraform等) を用いたインフラ構築 (可能な範囲で)
    *   [ ] デプロイ手順のドキュメント化とGitHub公開
2.  **[ ] テストコードの充実**
    *   [ ] 単体テスト (JUnit, Mockito) のカバレッジ向上
    *   [ ] 結合テスト (Spring Boot Test, Testcontainers) の拡充
    *   [ ] JaCoCoによるテストカバレッジレポート生成
3.  **[ ] パフォーマンスへの配慮と検証**
    *   [ ] 大量データインポート・検索時の性能測定 (JMH等)
    *   [ ] API応答速度の目標設定と測定 (Gatling等)
    *   [ ] 必要に応じたDBインデックス最適化、キャッシュ導入検討
4.  **[ ] CI/CDパイプラインの構築 (GitHub Actions)**
    *   [ ] テスト自動実行
    *   [ ] Dockerイメージビルドとプッシュ
    *   [ ] (可能であれば) クラウド環境への自動デプロイ

### 1.7 クリエイティブフェーズが必要なコンポーネント
- **[ ] UI/UXデザイン**:
    - メイン表示画面の具体的なレイアウト、視覚的表現（地合いの分かりやすさ）
    - 管理機能画面の操作性
- **[X] アーキテクチャ設計**:
    - バックエンドAPIの詳細なエンドポイント設計とリクエスト/レスポンス形式
    - データベーススキーマの最終決定（インデックス、リレーションシップなど）
    - 大量データ（5年分）の効率的な処理方法の検討（インポート、統計計算）
    - フロントエンドの状態管理戦略の詳細設計
- **[X] アルゴリズム設計**:
    - 地合い3分類アルゴリズムの具体的な閾値設定ロジック
    - 地合い判定ロジックの具体的な評価基準

### 1.8 依存関係
- MT5からのデータ出力形式（JSON）の安定性
- 各ライブラリ・フレームワークのバージョン互換性

### 1.9 課題と軽減策
- **課題1**: 開発期間が短い（2日間）。
    - **軽減策1**: 実績のある技術スタックを選定済み。機能を絞り込み、MVP（Minimum Viable Product）を意識して開発を進める。複雑な部分は段階的に実装する。
- **課題2**: 経済指標とボラティリティの紐付けロジックの複雑性。
    - **軽減策2**: `projectbrief.md`記載のロジックをベースに、テストデータを用いて早期に検証・改善を行う。
- **課題3**: 5年分の大量データ処理におけるパフォーマンス。
    - **軽減策3**: データベースのインデックス最適化、バッチ処理の検討、非同期処理の導入を検討する。
- **課題4**: フロントエンドとバックエンドの結合テスト。
    - **軽減策4**: API仕様(OpenAPI)を早期に確定し、モックサーバーやPostman等でバックエンドAPIを事前にテストする。フロントエンド側もモックデータでUI開発を進められるようにする。

### 1.10 検証
- [ ] 要件が計画にすべて反映されているか
- [ ] 技術スタックの検証ポイントが明確か
- [ ] 影響を受けるコンポーネントが特定されているか
- [X] 実装ステップが明確に定義されているか
- [ ] 依存関係と課題が文書化されているか
- [X] クリエイティブフェーズが必要なコンポーネントが特定されているか
- [X] `tasks.md` がこの計画で更新されたか

## Reflection Status
- [x] Implementation thoroughly reviewed
- [x] Successes documented
- [x] Challenges documented
- [x] Lessons learned documented
- [x] Process/Technical improvements identified

## Archive Status
- [x] Archive document created (`memory-bank/archive/archive-1.md`)
- [x] progress.md updated with archive status
- [x] activeContext.md reset 
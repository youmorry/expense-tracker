# リポジトリ構成

## 方針

- FE・BE をモノレポで管理する
- ルートで Docker Compose・CI 設定など共通の関心を管理する
- FE・BE はそれぞれ独立したプロジェクトとして `frontend/` / `backend/` に配置し、エディタで個別に開ける構成にする

---

## ディレクトリ構成

```
expense-tracker/
│
├── backend/                        # Spring Boot アプリケーション
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/expensetracker/
│   │   │   │   ├── domain/         # ドメインモデル（Entity, ValueObject, Repository interface）
│   │   │   │   ├── application/    # ユースケース（Service）
│   │   │   │   ├── infrastructure/ # DB・外部サービスの実装
│   │   │   │   └── presentation/   # REST Controller, DTO
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/   # Flyway マイグレーションファイル
│   │   └── test/
│   ├── build.gradle
│   └── Dockerfile                  # 本番デプロイ用（Render）
│
├── frontend/                       # React アプリケーション
│   ├── src/
│   │   ├── components/             # 共通UIコンポーネント
│   │   ├── features/               # 機能単位のモジュール
│   │   │   ├── transactions/       # 支出記録
│   │   │   └── analytics/          # 支出分析
│   │   ├── hooks/                  # カスタムフック
│   │   ├── lib/                    # API クライアント・ユーティリティ
│   │   └── types/                  # 型定義
│   ├── package.json
│   └── vite.config.ts
│
├── docs/                           # プロジェクトドキュメント
│   ├── 01-planning/
│   ├── 02-requirements/
│   └── 03-design/
│
├── .github/
│   └── workflows/
│       ├── backend-ci.yml          # BE のビルド・テスト（paths フィルター）
│       └── frontend-ci.yml         # FE のビルド・テスト（paths フィルター）
│
├── docker-compose.yml              # ローカル開発用 PostgreSQL 起動
├── CLAUDE.md                       # Claude Code 向けプロジェクト情報
└── README.md
```

---

## フロントエンド構成の補足

`features/` 配下を機能単位に分割する構成（Feature-Sliced Design の考え方を参考）。
各 feature は以下のようなサブディレクトリを持つことを基本とする。

```
features/transactions/
├── api/        # TanStack Query のフック（useTransactions など）
├── components/ # この機能固有のコンポーネント
└── types.ts    # この機能固有の型定義
```

---

## バックエンド構成の補足

DDD + レイヤードアーキテクチャをベースとしつつ、Spring Data JDBC の Aggregate / Repository の概念を活かす構成にする。

| パッケージ | 役割 |
|-----------|------|
| `domain` | Entity, ValueObject, Repository インターフェース。フレームワーク非依存 |
| `application` | ユースケースを実装する Service クラス |
| `infrastructure` | Spring Data JDBC による Repository 実装、外部 API クライアント |
| `presentation` | REST Controller, リクエスト・レスポンス DTO |

### 設計の意図

Spring 公式サンプルでよく見られる `controller / service / repository` の3層構成と比べ、
`domain` と `application` を明示的に分離している点が特徴。

| 観点 | 3層構成 | 今回の4層構成 |
|------|---------|-------------|
| ドメインロジックの置き場 | service に混在しやすい | domain に集約される |
| フレームワーク依存 | service が Spring に依存しがち | domain はフレームワーク非依存を保てる |
| 規模が大きくなったとき | 整理しにくくなる | 責務が明確なため変更しやすい |

Spring Data JDBC を採用した理由の一つが「DDD との親和性」であり、
Aggregate / Repository の概念をそのままコードに表現するためにこの構成を選んでいる。

### 層の名前の揺れ

文献やプロジェクトによって層の名前は異なるが、構造の考え方は共通している。

| このプロジェクト | よく見る別名 |
|----------------|------------|
| `domain` | `model` |
| `application` | `service`, `usecase` |
| `infrastructure` | `infra`, `repository` |
| `presentation` | `web`, `controller`, `api` |

---

## CI の設定方針

FE・BE が同一リポジトリでも、デプロイは個別にコントロールする。

| ワークフロー | トリガー | 内容 |
|------------|---------|------|
| `backend-ci.yml` | `backend/**` の変更 | Gradle ビルド・テスト |
| `frontend-ci.yml` | `frontend/**` の変更 | npm ビルド・lint |

Render 側も「Root Directory」を `backend/` / `frontend/` にそれぞれ設定することで、
デプロイを独立して管理する。

---

## エディタでの開き方

ルートを開けばモノレポ全体を俯瞰でき、サブディレクトリを開けば FE・BE 個別に作業できる。

| スタイル | 開き方 |
|---------|-------|
| 全体俯瞰 | `expense-tracker/` をルートとして開く |
| BE 専用 | `expense-tracker/backend/` をルートとして開く |
| FE 専用 | `expense-tracker/frontend/` をルートとして開く |

# Expense Tracker

個人向け支出記録・分析 Web アプリ。支出を「必要 (NEED) / 欲しい (WANT)」に分類し、カテゴリ別・分類別に分析できます。

> **Status**: 設計ドキュメント完了、実装フェーズ（MVP）

## Features

- **支出記録** — 日付・金額・カテゴリを入力してサッと記録
- **NEED / WANT 分類** — 各支出を「必要」「欲しい」に分類して振り返り
- **カテゴリ別分析** — ドーナツチャートで支出の内訳を可視化
- **NEED / WANT 比率** — 必要な支出と欲しい支出のバランスを把握
- **Google ログイン** — Google アカウントだけで利用開始、パスワード不要
- **多通貨対応** — locale から通貨を自動設定、設定画面から変更可能

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0.3 / Java 25 / Gradle (Kotlin DSL) |
| Frontend | React 19 / TypeScript 5 / Vite / Tailwind CSS |
| Data Access | Spring Data JDBC |
| State Management | TanStack Query v5 |
| Auth | Google OAuth2 + JWT (HS256) |
| Database | PostgreSQL 18 / Flyway |
| Hosting | Render (FE: Static Site, BE: Web Service) / Neon (DB) |

## Architecture

```
expense-tracker/
├── .devcontainer/    # Dev Container 設定
├── backend/          # Spring Boot API (DDD + Layered Architecture)
├── frontend/         # React SPA (Feature-Sliced Design)
├── docs/             # 設計ドキュメント
│   ├── 01-planning/
│   ├── 02-requirements/
│   └── 03-design/
└── docker-compose.yml
```

### Backend

DDD + レイヤードアーキテクチャ。Spring Data JDBC の Aggregate / Repository を DDD にそのまま対応させる設計。

```
com.youmorry.expensetracker/
├── domain/           # Entity, ValueObject, Repository interface
├── application/      # Service（ユースケース）
├── infrastructure/   # Spring Data JDBC 実装, 外部 API
└── presentation/     # REST Controller, DTO
```

### Frontend

Feature-Sliced Design で機能単位にモジュールを管理。

```
src/
├── components/       # 共通 UI コンポーネント
├── features/         # 機能単位モジュール（transactions, analytics）
├── hooks/            # カスタムフック
├── lib/              # API クライアント・ユーティリティ
└── types/            # 共通型定義
```

## Getting Started

### Dev Container（推奨）

VS Code / GitHub Codespaces の [Dev Containers](https://containers.dev/) に対応しています。Java 25・Node.js 22・Claude Code が事前構成済みのため、環境構築なしですぐに開発を始められます。

1. VS Code で **Dev Containers** 拡張機能をインストール
2. コマンドパレットから **Dev Containers: Reopen in Container** を実行

プリインストールされる VS Code 拡張機能:

- Java Extension Pack / Spring Boot Extension
- ESLint / Prettier / Tailwind CSS IntelliSense
- Claude Code

### ローカル環境

Dev Container を使わない場合は、以下を手動でセットアップしてください。

#### Prerequisites

- Java 25
- Node.js
- Docker

#### Setup

```bash
# ローカル DB 起動
docker compose up -d

# Backend
cd backend
./gradlew bootRun

# Frontend (別ターミナル)
cd frontend
npm install
npm run dev
```

### Build & Test

```bash
# Backend
cd backend
./gradlew test
./gradlew build

# Frontend
cd frontend
npm test
npm run build
```

## Docker Compose

ローカル開発用に PostgreSQL と Swagger UI を提供します。

```bash
docker compose up -d
```

| Service | Description | Port |
|---------|-------------|------|
| db | PostgreSQL 18 | `localhost:5432` |
| mock-server | Prism（OpenAPI モックサーバー） | `localhost:4010` |
| swagger-ui | Swagger UI（OpenAPI ビューア） | `localhost:8081` |

**DB 接続情報**

| Key | Value |
|-----|-------|
| Host | `localhost` |
| Port | `5432` |
| Database | `expense_tracker` |
| User | `postgres` |
| Password | `postgres` |

データは Docker ボリューム `db-data` に永続化されます。リセットする場合:

```bash
docker compose down -v
```

**ブランチ切り替え時の DB リセット**

Flyway マイグレーションはバージョン番号で管理されるため、ブランチ間でマイグレーションが異なると起動時にエラーになることがあります。ブランチを切り替えた際は DB をリセットし、`bootRun` でマイグレーションを再適用してください。

```bash
# 1. DB をボリュームごと削除して再作成
docker compose down -v && docker compose up -d

# 2. マイグレーションを適用（bootRun 起動時に Flyway が自動実行）
cd backend && ./gradlew bootRun
```

## Design Documents

設計の詳細は [docs/](docs/) を参照してください。

| Document | Content |
|----------|---------|
| [API Design](docs/03-design/api-design.md) | 全 12 エンドポイントの API 仕様 |
| [Auth Design](docs/03-design/auth-design.md) | 認証フロー・JWT・セキュリティ |
| [Database Schema](docs/03-design/database-schema.md) | DDL・インデックス・Flyway |
| [Domain Model](docs/03-design/domain-model.md) | Entity / ValueObject / Aggregate 設計 |
| [Error Handling](docs/03-design/error-handling.md) | 例外階層・RFC 9457 レスポンス |
| [ER Diagram](docs/03-design/er-diagram.md) | ER 図・テーブル構成 |
| [Repository Structure](docs/03-design/repository-structure.md) | モノレポ構成・ディレクトリ設計 |
| [Screen Flow](docs/03-design/screen-flow.md) | UI 画面遷移・インタラクション |
| [Java Coding Standards](docs/java-coding-standards.md) | Java コーディング規約（Google Style） |

## License

Private

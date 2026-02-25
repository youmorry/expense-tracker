# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Expense Tracker — 個人向け支出記録・分析 Web アプリ。支出を「必要 (NEED) / 欲しい (WANT)」に分類し、カテゴリ別・分類別に分析する。モノレポ構成で FE / BE を管理する。

**ステータス**: 設計ドキュメント完了、実装未着手（MVP フェーズ）

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 4.0, Java 25 (LTS), Gradle (Kotlin DSL) |
| Frontend | React 19, TypeScript 5, Vite, Tailwind CSS |
| Data Access | Spring Data JDBC (JPA ではなく DDD 親和性のため採用) |
| State Management | TanStack Query (React Query v5) |
| Auth | Google OAuth2 only, JWT (HS256, メモリ保持) |
| Database | PostgreSQL 18, Flyway migration |
| Hosting | Render (FE: Static Site, BE: Web Service), Neon (DB) |

## Build & Dev Commands

```bash
# ローカル DB 起動
docker compose up

# Backend
cd backend
./gradlew bootRun           # 開発サーバー起動
./gradlew test              # テスト実行
./gradlew build             # ビルド

# Frontend
cd frontend
npm install
npm run dev                 # 開発サーバー起動
npm test                    # テスト実行
npm run build               # ビルド
```

## Architecture

### Monorepo Structure

```
expense-tracker/
├── backend/          # Spring Boot API
├── frontend/         # React SPA
├── docs/             # 設計ドキュメント
│   ├── 01-planning/
│   ├── 02-requirements/
│   └── 03-design/
└── docker-compose.yml
```

### Backend — DDD + Layered Architecture

```
com.example.expensetracker/
├── domain/           # Entity, ValueObject, Repository interface（フレームワーク非依存）
├── application/      # Service（ユースケース実装）
├── infrastructure/   # Spring Data JDBC Repository 実装, 外部 API
└── presentation/     # REST Controller, DTO
```

`domain` と `application` を明示的に分離し、ドメインロジックを `domain` に集約する。Spring Data JDBC の Aggregate/Repository 概念を DDD にそのまま対応させる設計。

### Frontend — Feature-Sliced Design

```
src/
├── components/       # 共通 UI コンポーネント
├── features/         # 機能単位モジュール
│   ├── transactions/ # 支出記録（api/, components/, types.ts）
│   └── analytics/    # 支出分析
├── hooks/            # カスタムフック
├── lib/              # API クライアント・ユーティリティ
└── types/            # 共通型定義
```

## API Design Conventions

- RESTful, JSON, `/api/v1/` prefix
- snake_case（JSON キー・クエリパラメータ）、URL リソース名は複数形
- 金額は String 型（浮動小数点誤差回避）
- 日付は ISO 8601
- null の場合はキーを省略（null ではなく undefined）
- エラーレスポンスは RFC 9457 Problem Details 準拠
- 認証: `Authorization: Bearer {JWT}`
- 他ユーザーのリソースアクセスは 404 を返す（403 ではなく、存在を隠す）

## Auth Flow

1. FE: Google OAuth2 で ID トークン取得（クライアントサイド）
2. FE → BE: `POST /api/v1/auth/google` に ID トークン送信
3. BE: Google JWKS で署名検証 → ユーザー検索/作成（新規時は locale → 通貨推定）
4. BE → FE: JWT アクセストークン返却（24 時間有効）
5. FE: JWT をメモリに保持（localStorage/Cookie 不使用）

## Key Design Decisions

- **Spring Data JDBC over JPA**: 暗黙の挙動（遅延ロード・ダーティチェック）を排除し SQL を透明に
- **Google OAuth2 only**: ユーザー登録・パスワード管理の複雑さを排除
- **JWT in memory**: XSS でのトークン窃取リスクを最小化（CSRF も不要に）
- **HS256 (symmetric JWT)**: 単一サーバー構成のためシンプルな対称鍵で十分
- **Pagination なし**: 個人利用で年間 ~3000 件程度のため全件取得で対応

## Conventions

- コミットメッセージ: `<type>: <subject>`（日本語可、例: `docs: 認証設計資料を追加`）
- ドキュメント: 日本語、コード: 英語
- DB カラム / JSON キー: snake_case、Java / TypeScript: camelCase

## Java Coding Standards

[docs/java-coding-standards.md](docs/java-coding-standards.md) を参照。

## Key Design Documents

| Document | Content |
|----------|---------|
| [api-design.md](docs/03-design/api-design.md) | 全 12 エンドポイントの API 仕様 |
| [auth-design.md](docs/03-design/auth-design.md) | 認証フロー・JWT・セキュリティ |
| [database-schema.md](docs/03-design/database-schema.md) | DDL・インデックス・Flyway |
| [domain-model.md](docs/03-design/domain-model.md) | Entity/ValueObject・Aggregate 設計 |
| [error-handling.md](docs/03-design/error-handling.md) | 例外階層・RFC 9457 レスポンス |
| [screen-flow.md](docs/03-design/screen-flow.md) | UI 画面遷移・インタラクション |
| [repository-structure.md](docs/03-design/repository-structure.md) | ディレクトリ構成・CI 方針 |
| [tech-stack.md](docs/01-planning/tech-stack.md) | 技術選定理由 |

# 技術スタック

## 概要

| レイヤー | 技術 | バージョン |
|----------|------|------------|
| バックエンド | Spring Boot | 4.0.x |
| バックエンド言語 | Java | 25 (LTS) |
| フロントエンド | React | 19.x |
| フロントエンド言語 | TypeScript | 5.x |
| データベース | PostgreSQL | 18.x |
| コンテナ | Docker / Docker Compose | - |

---

## バックエンド

### コアフレームワーク

| 技術 | 用途 |
|------|------|
| Spring Boot 4.0 | アプリケーションフレームワーク |
| Spring Web | REST API 提供 |
| Spring Data JDBC | データアクセス |
| Spring Security | 認証・認可 |
| Spring OAuth2 Resource Server | JWT 検証 |

> **Spring Boot 4.0 について**
> 2025年11月リリースの最新メジャーバージョン。Spring Framework 7.0 / Jakarta EE 11 ベース。
> Jackson 3 への移行（パッケージが `com.fasterxml.jackson` → `tools.jackson`）など破壊的変更があるため、
> ライブラリの対応状況を確認しながら進める。

### 認証

| 技術 | 用途 |
|------|------|
| Google OAuth2 | ソーシャルログイン |
| Spring Security OAuth2 Client | OAuth2 フロー処理 |

ユーザー登録・パスワード管理の複雑さを排除するため、Google認証のみサポートする。

### データベース・マイグレーション

| 技術 | 用途 |
|------|------|
| PostgreSQL 18 | リレーショナルDB |
| Flyway | DBマイグレーション管理 |

### API仕様

| 技術 | 用途 |
|------|------|
| SpringDoc OpenAPI | OpenAPI 3.0 仕様の自動生成 |
| Swagger UI | API ドキュメント閲覧 |

---

## フロントエンド

### コアフレームワーク

| 技術 | 用途 |
|------|------|
| React 19 | UI フレームワーク |
| TypeScript 5 | 型安全な開発 |
| Vite | ビルドツール・開発サーバー |

### 状態管理・データフェッチ

| 技術 | 用途 |
|------|------|
| TanStack Query (React Query v5) | サーバー状態管理・キャッシュ |

> **TanStack Query について**
> APIから取得するデータの「ローディング・エラー・キャッシュ・再取得」をまとめて管理するライブラリ。
> `useState` + `useEffect` によるデータフェッチの定型コードを削減できる。

### スタイリング

| 技術 | 用途 |
|------|------|
| Tailwind CSS | ユーティリティファーストCSS |

### 認証

| 技術 | 用途 |
|------|------|
| @react-oauth/google | Google OAuth2 クライアント |

---

## 開発環境

| 技術 | 用途 |
|------|------|
| Docker Compose | ローカル開発環境の構築（DB等） |
| Git / GitHub | バージョン管理 |
| GitHub Issues / Projects | タスク管理 |

---

## CI/CD（将来対応）

| 技術 | 用途 |
|------|------|
| GitHub Actions | 自動テスト・デプロイ |

---

## 技術選定の理由

### バックエンド: Spring Boot 4.0 + Java 25 (LTS)
既存のJava経験を活かしつつ、最新LTSであるJava 25と最新のSpring Boot 4.0を採用する。

### フロントエンド: React 19 + TypeScript
React 19は2024年12月に正式リリースされた安定版。Actions API・Server Components（フレームワーク経由）・フォーム処理の改善など実用的な機能が追加されている。TypeScriptにより型安全性を確保し、保守性を高める。

### データアクセス: Spring Data JDBC
JPAと比較して以下の理由から採用する。

- **学習コストが低い**: 遅延ロード・ダーティチェック・一次キャッシュなどJPA固有の暗黙の挙動がなく、
  「SQLが明示的に発行される」シンプルなモデルを維持できる。
- **DDDとの親和性**: Aggregate / Repository の概念がDDDのそれと直接対応しており、
  ドメインモデルをAggregateRoot単位で設計する思想を自然に表現できる。
- **SQLの透明性**: 発行されるSQLが予測しやすく、クエリのデバッグや最適化が容易。

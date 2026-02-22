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

### ローカル開発

| 技術 | 用途 |
|------|------|
| Docker Compose | PostgreSQL などのミドルウェアをローカルで起動 |
| Git / GitHub | バージョン管理 |
| GitHub Issues / Projects | タスク管理 |

ローカル開発では、アプリ本体（Spring Boot / React）はホストマシン上で直接起動し、
PostgreSQL のみ Docker Compose で管理する。これにより開発時のビルド・起動サイクルを高速に保つ。

---

## インフラ・デプロイ

### 構成

```
FE: Render（Static Site）   ← 無料・永続・CDN配信
BE: Render（Web Service）   ← 無料・15分無通信でスリープあり
DB: Neon                    ← PostgreSQL マネージドサービス・無料・永続
```

| サービス | 役割 | 無料枠の制限 |
|----------|------|-------------|
| Render Static Site | FE（React）の配信 | 制限なし |
| Render Web Service | BE（Spring Boot）の実行 | 15分無通信でスリープ（復帰に30〜60秒） |
| Neon | DB（PostgreSQL）のホスティング | 制限なし（スリープは数百ms程度で実用上問題なし） |

> **スリープについて**
> Render 無料枠の BE は15分無通信でスリープ状態になる。無料運用の制約として許容する。
> 将来的に常時起動が必要になった場合は Render の有料プラン（$7/月〜）に移行する。

### デプロイフロー

```
main にマージ
  ├─→ Render: FE を自動ビルド（npm run build）→ CDN にデプロイ
  └─→ Render: BE を自動ビルド（Dockerfile）→ コンテナ起動
           └─→ Flyway マイグレーション実行 → アプリ起動

DB（Neon）はデプロイとは独立して管理（スキーマ変更は Flyway 経由）
```

### 環境変数

コードに書かず、Render のダッシュボードで設定する。

| 変数名 | 設定先 | 内容 |
|--------|--------|------|
| `DB_URL` | BE | Neon の接続URL |
| `DB_USER` | BE | DB ユーザー名 |
| `DB_PASSWORD` | BE | DB パスワード |
| `GOOGLE_CLIENT_ID` | BE | OAuth2 クライアントID |
| `GOOGLE_CLIENT_SECRET` | BE | OAuth2 クライアントシークレット |
| `VITE_API_BASE_URL` | FE | BE の URL |

### Docker の利用範囲

| 環境 | 構成 |
|------|------|
| ローカル | Docker Compose で PostgreSQL のみ起動。アプリはホストマシンで直接実行 |
| 本番（Render） | BE は Dockerfile でコンテナ化してデプロイ。DB は Neon のマネージドサービスを利用 |

---

## CI/CD

| 技術 | 用途 |
|------|------|
| GitHub Actions | テスト自動化 |
| Render | GitHub 連携による自動デプロイ（CD） |

### パイプライン構成

```
PR 作成 → GitHub Actions: ビルド + テスト（CI）
main マージ → Render: 自動デプロイ（CD）
```

| ステージ | 実行環境 | 内容 |
|----------|----------|------|
| ビルド | GitHub Actions | Spring Boot jar ビルド / React ビルド |
| テスト | GitHub Actions | 単体テスト・結合テストの自動実行 |
| デプロイ | Render | main マージをトリガーに FE・BE を自動デプロイ |

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

### インフラ: Render + Neon
無料で永続運用できる構成として採用。FE は Render Static Site でCDN配信、BE は Render Web Service で
Dockerコンテナ実行、DB は Neon のマネージドPostgreSQLを利用する。BE のスリープは無料運用の制約として許容する。
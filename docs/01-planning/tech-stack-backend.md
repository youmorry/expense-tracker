# 技術スタック — バックエンド

## コアフレームワーク

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

## 認証

| 技術 | 用途 |
|------|------|
| Google OAuth2 | ソーシャルログイン |
| Spring Security OAuth2 Client | OAuth2 フロー処理 |

ユーザー登録・パスワード管理の複雑さを排除するため、Google認証のみサポートする。

## ビルドツール

| 技術 | 用途 |
|------|------|
| Gradle (Kotlin DSL) | ビルド・依存関係管理 |

Maven と比較してビルドが速く（インクリメンタルビルド・キャッシュ）、設定の記述量も少ない。
Spring Boot の新規プロジェクトで採用が増えており、Spring Initializr のデフォルトでもある。
スクリプトは Kotlin DSL（`build.gradle.kts`）を使用し、IDE の補完を活用する。

## データベース・マイグレーション

| 技術 | 用途 |
|------|------|
| PostgreSQL 18 | リレーショナルDB |
| Flyway | DBマイグレーション管理 |

## API仕様

| 技術 | 用途 |
|------|------|
| SpringDoc OpenAPI | OpenAPI 3.0 仕様の自動生成 |
| Scalar | API ドキュメント閲覧 |

---

## 技術選定の理由

### Spring Boot 4.0 + Java 25 (LTS)

既存のJava経験を活かしつつ、最新LTSであるJava 25と最新のSpring Boot 4.0を採用する。

### Gradle (Kotlin DSL)

Mavenと比較してビルドが速く、設定の記述量も少ない。Spring Boot の新規プロジェクトで採用が増えており、Kotlin DSL により IDE の補完が効いて書きやすい。

### Spring Data JDBC

JPAと比較して以下の理由から採用する。

- **学習コストが低い**: 遅延ロード・ダーティチェック・一次キャッシュなどJPA固有の暗黙の挙動がなく、
  「SQLが明示的に発行される」シンプルなモデルを維持できる。
- **DDDとの親和性**: Aggregate / Repository の概念がDDDのそれと直接対応しており、
  ドメインモデルをAggregateRoot単位で設計する思想を自然に表現できる。
- **SQLの透明性**: 発行されるSQLが予測しやすく、クエリのデバッグや最適化が容易。
